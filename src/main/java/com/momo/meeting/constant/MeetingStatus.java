package com.momo.meeting.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MeetingStatus {

  RECRUITING("모집 중"),
  CLOSED("모집 완료");

  private final String description;
}
