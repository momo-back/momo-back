package com.momo.notification.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

  // 모임 관련 (주최자)
  NEW_PARTICIPATION_REQUEST("새로운 참가 신청", NotificationCategory.MEETING),

  PARTICIPANT_LEFT("참가자 탈퇴", NotificationCategory.MEETING),

  // 모임 관련 (참가자)
  PARTICIPANT_APPROVED("참가 승인", NotificationCategory.MEETING),

  PARTICIPANT_REJECTED("참가 거절", NotificationCategory.MEETING),

  PARTICIPANT_KICKED("강퇴", NotificationCategory.MEETING),

  PARTICIPANT_MEETING_CLOSED("모임 종료", NotificationCategory.MEETING),

  // 채팅 관련
  NEW_CHAT_MESSAGE("새로운 채팅", NotificationCategory.CHAT);

  private final String description;
  private final NotificationCategory category;
}
