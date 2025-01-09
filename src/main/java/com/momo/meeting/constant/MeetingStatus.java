package com.momo.meeting.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MeetingStatus {

  RECRUITING("모집 중"),
  CLOSED("모집 완료");

  // 모집 가능 상태인지 확인하는 메서드
  public boolean isParticipate() {
    return this == RECRUITING;
  }

  private final String description;
}
