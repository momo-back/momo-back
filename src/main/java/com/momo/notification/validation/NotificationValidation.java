package com.momo.notification.validation;

import com.momo.notification.entity.Notification;
import com.momo.notification.exception.NotificationErrorCode;
import com.momo.notification.exception.NotificationException;
import com.momo.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationValidation {

  private final NotificationRepository notificationRepository;

  public Notification validateNotification(Long notificationId, Long receiverId) {
    return notificationRepository.findByIdAndReceiver_Id(notificationId, receiverId)
        .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));
  }
}
