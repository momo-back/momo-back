package com.momo.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.momo.notification.constant.NotificationType;
import com.momo.notification.dto.NotificationResponse;
import com.momo.notification.entity.Notification;
import com.momo.notification.repository.NotificationRepository;
import com.momo.notification.sseemitter.SseEmitterManager;
import com.momo.user.dto.CustomUserDetails;
import com.momo.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock
  private SseEmitterManager sseEmitterManager;

  @Mock
  private NotificationRepository notificationRepository;

  @InjectMocks
  private NotificationService notificationService;

  @Test
  @DisplayName("SSE 구독 - 성공")
  void subscribe_Success() {
    // given
    CustomUserDetails customUserDetails = new CustomUserDetails(createUser());

    // when
    SseEmitter sseEmitter = notificationService.subscribeSseEmitter(customUserDetails.getId());

    // then
    assertThat(sseEmitter).isNotNull();

    // emitter 가 add 메서드를 통해 저장되었는지 확인
    verify(sseEmitterManager).add(eq(customUserDetails.getId()), any(SseEmitter.class));
  }

  @Test
  @DisplayName("알림 전송 - 성공")
  void sendNotification_Success() {
    // given
    User user = createUser();
    SseEmitter emitter = new SseEmitter();
    when(sseEmitterManager.get(user.getId())).thenReturn(Optional.of(emitter));
    when(notificationRepository.existsByUser_Id(user.getId())).thenReturn(true);

    // when
    notificationService.sendNotification(
        user, "테스트 알림", NotificationType.NEW_CHAT_MESSAGE);

    // then
    verify(notificationRepository).save(any(Notification.class));
  }

  @Test
  @DisplayName("알림 목록 조회 - 성공")
  void getNotifications_Success() {
    // given
    Long userId = 1L;
    List<Notification> notifications = List.of(
        createNotification("알림1"),
        createNotification("알림2")
    );
    when(notificationRepository.findAllByUser_Id(userId)).thenReturn(notifications);

    // when
    List<NotificationResponse> result = notificationService.getNotifications(userId);

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getContent()).isEqualTo("알림1");
    assertThat(result.get(1).getContent()).isEqualTo("알림2");
  }

  @Test
  @DisplayName("특정 알림 삭제 - 성공")
  void deleteNotification_Success() {
    // given
    Long notificationId = 1L;
    Long userId = 1L;
    Notification notification = createNotification("테스트 알림");

    when(notificationRepository.deleteByIdAndUser_Id(notificationId, userId)).thenReturn(1);
    when(notificationRepository.existsByUser_Id(userId)).thenReturn(false);

    // when
    notificationService.deleteNotification(notificationId, userId);

    // then
    verify(notificationRepository).deleteByIdAndUser_Id(notificationId, userId);
    verify(notificationRepository).existsByUser_Id(userId);
  }

  @Test
  @DisplayName("전체 알림 삭제 - 성공")
  void deleteAllNotifications_Success() {
    // given
    Long userId = 1L;
    when(notificationRepository.existsByUser_Id(userId)).thenReturn(false);

    // when
    notificationService.deleteAllNotifications(userId);

    // then
    verify(notificationRepository).deleteAllByUser_Id(userId);
  }

  private static User createUser() {
    return User.builder()
        .id(1L)
        .email("test@gmail.com")
        .password("testapssword")
        .phone("01012345678")
        .enabled(true)
        .verificationToken("asdasdsad")
        .build();
  }

  private static Notification createNotification(String content) {
    return Notification.builder()
        .content(content)
        .user(createUser())
        .notificationType(NotificationType.NEW_PARTICIPATION_REQUEST)
        .build();
  }
}