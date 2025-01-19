package com.momo.notification.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

  // 모임 관련 (주최자)
  NEW_PARTICIPATION_REQUEST(
      "님이 모임 참여를 신청했습니다.", NotificationCategory.MEETING),

  PARTICIPANT_LEFT("참가자 탈퇴", NotificationCategory.MEETING),

  MEETING_EXPIRED(
      "모임 시간이 만료되어 자동으로 삭제되었습니다.", NotificationCategory.MEETING),

  // 모임 관련 (참가자)
  PARTICIPANT_APPROVED("모임에 참여가 승인되었습니다.", NotificationCategory.MEETING),

  PARTICIPANT_REJECTED("모임에 참여가 거절되었습니다.", NotificationCategory.MEETING),

  PARTICIPANT_KICKED("강퇴", NotificationCategory.MEETING),

  PARTICIPANT_MEETING_CLOSED("모임 종료", NotificationCategory.MEETING),

  // 채팅 관련
  NEW_CHAT_MESSAGE("새로운 채팅", NotificationCategory.CHAT);

  private final String description;
  private final NotificationCategory category;
}
