package com.momo.meeting.constant;

import java.util.EnumSet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MeetingStatus {

  RECRUITING("모집 중"),
  CLOSED("모집 완료");

  // 상태가 추가될 것을 대비하여 모집 가능 상태와 모집 불가 상태를 구분
  private static final EnumSet<MeetingStatus> ENABLE_RECRUITING = EnumSet.of(RECRUITING);
  private static final EnumSet<MeetingStatus> UNABLE_RECRUITING = EnumSet.of(CLOSED);

  // 모집 가능 상태인지 확인하는 메서드
  public boolean isParticipate() {
    return ENABLE_RECRUITING.contains(this);
  }

  private final String description;
}
