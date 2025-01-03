package com.momo.notification.validation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.momo.notification.exception.NotificationErrorCode;
import com.momo.notification.exception.NotificationException;
import com.momo.notification.repository.NotificationRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationValidationTest {

  @Mock
  private NotificationRepository notificationRepository;

  @InjectMocks
  private NotificationValidation notificationValidation;

  @Test
  @DisplayName("존재하지 않는 알림 검색 - 예외 발생")
  void validateNotification_NotificationNotFound_ThrowsException() {
    // given
    Long notificationId = 1L;
    Long receiverId = 1L;
    when(notificationRepository.findByIdAndReceiver_Id(notificationId, receiverId))
        .thenReturn(Optional.empty());

    // when
    // then
    assertThatThrownBy(
        () -> notificationValidation.validateNotification(notificationId, receiverId))
        .isInstanceOf(NotificationException.class)
        .hasFieldOrPropertyWithValue(
            "notificationErrorCode",
            NotificationErrorCode.NOTIFICATION_NOT_FOUND
        );

    verify(notificationRepository).findByIdAndReceiver_Id(notificationId, receiverId);
  }
}