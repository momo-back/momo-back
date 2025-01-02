package com.momo.participation.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ParticipationStatus {
  PENDING("승인 대기"),
  APPROVED("승인 완료"),
  REJECTED("승인 거부"),
  CLOSED("모집 완료"),
  CANCELED("모집 취소");

  private final String description;
}
