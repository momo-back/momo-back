package com.momo.notification.dto;

import com.momo.notification.constant.NotificationType;
import com.momo.notification.entity.Notification;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponse {

  private Long id;
  private String content;
  private NotificationType notificationType;

  public static NotificationResponse from(Notification notification) {
    return NotificationResponse.builder()
        .id(notification.getId())
        .content(notification.getContent())
        .notificationType(notification.getNotificationType())
        .build();
  }
}
