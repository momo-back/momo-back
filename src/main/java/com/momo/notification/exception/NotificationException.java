package com.momo.notification.exception;

import lombok.Getter;

@Getter
public class NotificationException extends RuntimeException {

  private final NotificationErrorCode notificationErrorCode;

  public NotificationException(NotificationErrorCode notificationErrorCode) {
    super(notificationErrorCode.getMessage());
    this.notificationErrorCode = notificationErrorCode;
  }
}
