package com.momo.notification.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationCategory {

  CHAT("채팅 알림"),
  MEETING("모임 알림");

  private final String description;
}
