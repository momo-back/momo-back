package com.momo.notification.service;

import com.momo.notification.sseemitter.SseEmitterManager;
import com.momo.notification.constant.NotificationType;
import com.momo.notification.dto.NotificationResponse;
import com.momo.notification.entity.Notification;
import com.momo.notification.repository.NotificationRepository;
import com.momo.notification.validation.NotificationValidation;
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
  private final NotificationValidation notificationValidation;
  private final SseEmitterManager sseEmitterManager;

  public SseEmitter subscribeSseEmitter(Long receiverId) {
    SseEmitter sseEmitter = new SseEmitter(SseEmitterManager.sseTimeout); // 타임아웃 설정

    // 콜백 등록
    sseEmitter.onCompletion(() -> sseEmitterManager.remove(receiverId)); // 연결 종료 시 이미터 제거
    sseEmitter.onTimeout(() -> sseEmitterManager.remove(receiverId)); // 타임아웃 발생 시 이미터 제거

    sseEmitterManager.add(receiverId, sseEmitter); // 새로운 이미터를 저장소에 추가

    try {
      // 연결 직후 테스트용 이벤트 전송
      sseEmitter.send(SseEmitter.event()
          .name("connect")
          .data("connected!"));
    } catch (IOException e) {
      sseEmitterManager.remove(receiverId); // 전송 실패 시 이미터 제거
      // 전송 실패는 대부분 클라이언트 끊김을 의미. 재전송 시도하지 않음.
    }
    return sseEmitter; // 생성된 이미터 반환
  }

  /**
   * 새로운 알림을 생성하고 실시간으로 전송하는 메서드. 알림을 보내야 하는 서비스 쪽에서 호출하여 사용.
   *
   * @param receiver         알림을 받을 사용자
   * @param notificationType 알림 타입
   * @param content          알림 내용
   */
  @Transactional
  public void sendNotification(User receiver, String content, NotificationType notificationType) {
    Notification notification = createNotification(receiver, content, notificationType);
    notificationRepository.save(notification);

    trySendNotification(receiver, notificationType, notification); // 실시간 알림 전송 시도
    boolean hasNotifications = notificationRepository.existsByReceiver_Id(receiver.getId());
    tryNotifyNotificationStatus(receiver.getId(), hasNotifications);
  }

  @Transactional(readOnly = true)
  public List<NotificationResponse> getNotifications(Long receiverId) {
    return notificationRepository.findAllByReceiver_Id(receiverId)
        .stream()
        .map(NotificationResponse::from)
        .collect(Collectors.toList());
  }

  @Transactional
  public void deleteNotification(Long notificationId, Long receiverId) {
    Notification notification = notificationValidation.validateNotification(
        notificationId, receiverId);

    notificationRepository.delete(notification);
    boolean hasNotifications = notificationRepository.existsByReceiver_Id(receiverId);
    tryNotifyNotificationStatus(receiverId, hasNotifications);
  }

  @Transactional
  public void deleteAllNotifications(Long receiverId) {
    notificationRepository.deleteAllByReceiver_Id(receiverId);
    boolean hasNotifications = notificationRepository.existsByReceiver_Id(receiverId);
    tryNotifyNotificationStatus(receiverId, hasNotifications);
  }

  private void trySendNotification(
      User receiver, NotificationType notificationType, Notification notification
  ) {
    sseEmitterManager.get(receiver.getId())
        .ifPresent(sseEmitter -> {
          try {
            sseEmitter.send(SseEmitter.event()
                .name(notificationType.name()) // 이벤트 이름으로 알림 타입 사용
                .data(notification.getContent())); // 알림 데이터 전송
          } catch (IOException e) {
            // 전송 실패 시 연결 제거
            sseEmitterManager.remove(receiver.getId());
          }
        });
  }

  private void tryNotifyNotificationStatus(Long receiverId, boolean hasNotifications) {
    sseEmitterManager.get(receiverId)
        .ifPresent(emitter -> {
          try { // 알림 상태를 클라이언트에 전송
            emitter.send(SseEmitter.event().data(Map.of("hasNotifications", hasNotifications)));
          } catch (IOException e) {
            sseEmitterManager.remove(receiverId); // 에러 발생 시 연결 해제
          }
        });
  }

  private static Notification createNotification(
      User receiver, String content, NotificationType notificationType
  ) {
    return Notification.builder()
        .content(content)
        .receiver(receiver)
        .notificationType(notificationType)
        .build();
  }
}
