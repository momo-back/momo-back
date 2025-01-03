package com.momo.notification.service;

import com.momo.notification.exception.NotificationErrorCode;
import com.momo.notification.exception.NotificationException;
import com.momo.notification.sseemitter.SseEmitterManager;
import com.momo.notification.constant.NotificationType;
import com.momo.notification.dto.NotificationResponse;
import com.momo.notification.entity.Notification;
import com.momo.notification.repository.NotificationRepository;
import com.momo.user.entity.User;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final SseEmitterManager sseEmitterManager;

  public SseEmitter subscribeSseEmitter(Long userId) {
    SseEmitter sseEmitter = new SseEmitter(SseEmitterManager.sseTimeout); // 타임아웃 설정

    // 콜백 등록
    sseEmitter.onCompletion(() -> sseEmitterManager.remove(userId)); // 연결 종료 시 이미터 제거
    sseEmitter.onTimeout(() -> sseEmitterManager.remove(userId)); // 타임아웃 발생 시 이미터 제거

    sseEmitterManager.add(userId, sseEmitter); // 새로운 이미터를 저장소에 추가

    try {
      // 연결 직후 테스트용 이벤트 전송
      sseEmitter.send(SseEmitter.event()
          .name("connect")
          .data("connected!"));
    } catch (IOException e) {
      sseEmitterManager.remove(userId); // 전송 실패 시 이미터 제거
      // 전송 실패는 대부분 클라이언트 끊김을 의미. 재전송 시도하지 않음.
    }
    return sseEmitter; // 생성된 이미터 반환
  }

  /**
   * 새로운 알림을 생성하고 실시간으로 전송하는 메서드. 알림을 보내야 하는 서비스 쪽에서 호출하여 사용.
   *
   * @param user             알림 수신자
   * @param notificationType 알림 타입
   * @param content          알림 내용
   */
  @Transactional
  public void sendNotification(User user, String content, NotificationType notificationType) {
    Notification notification = createNotification(user, content, notificationType);
    notificationRepository.save(notification);

    trySendNotification(user, notificationType, notification); // 실시간 알림 전송 시도
    boolean hasNotifications = notificationRepository.existsByUser_Id(user.getId());
    tryNotifyNotificationStatus(user.getId(), hasNotifications);
  }

  @Transactional(readOnly = true)
  public List<NotificationResponse> getNotifications(Long userId) {
    return notificationRepository.findAllByUser_Id(userId)
        .stream()
        .map(NotificationResponse::from)
        .collect(Collectors.toList());
  }

  @Transactional
  public void deleteNotification(Long userId, Long notificationId) {
    int deletedCount = notificationRepository.deleteByIdAndUser_Id(notificationId, userId);
    if (deletedCount == 0) {
      throw new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
    }
    boolean hasNotifications = notificationRepository.existsByUser_Id(userId);
    tryNotifyNotificationStatus(userId, hasNotifications);
  }

  @Transactional
  public void deleteAllNotifications(Long userId) {
    notificationRepository.deleteAllByUser_Id(userId);
    boolean hasNotifications = notificationRepository.existsByUser_Id(userId);
    tryNotifyNotificationStatus(userId, hasNotifications);
  }

  private void trySendNotification(
      User user, NotificationType notificationType, Notification notification
  ) {
    sseEmitterManager.get(user.getId())
        .ifPresent(sseEmitter -> {
          try {
            sseEmitter.send(SseEmitter.event()
                .name(notificationType.name()) // 이벤트 이름으로 알림 타입 사용
                .data(notification.getContent())); // 알림 데이터 전송
          } catch (IOException e) {
            // 전송 실패 시 연결 제거
            sseEmitterManager.remove(user.getId());
          }
        });
  }

  private void tryNotifyNotificationStatus(Long userId, boolean hasNotifications) {
    sseEmitterManager.get(userId)
        .ifPresent(emitter -> {
          try { // 알림 상태를 클라이언트에 전송
            emitter.send(SseEmitter.event().data(Map.of("hasNotifications", hasNotifications)));
          } catch (IOException e) {
            sseEmitterManager.remove(userId); // 에러 발생 시 연결 해제
          }
        });
  }

  private static Notification createNotification(
      User user, String content, NotificationType notificationType
  ) {
    return Notification.builder()
        .content(content)
        .user(user)
        .notificationType(notificationType)
        .build();
  }
}
