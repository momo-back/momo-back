package com.momo.participation.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ParticipationStatus {
  PENDING("승인 대기"), // 모임 신청 취소 가능
  APPROVED("승인 완료"), // 모임(채팅방) 나가기 가능
  REJECTED("승인 거부"), // 참여한 모집글 목록에서 제거 가능
  CLOSED("모집 완료"), // 참여한 모집글 목록에서 제거 가능
  CANCELED("모집 취소"); // 참여한 모집글 목록에서 제거 가능

  private final String description;
}
