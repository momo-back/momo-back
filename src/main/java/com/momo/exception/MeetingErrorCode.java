package com.momo.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MeetingErrorCode {

  DAILY_POSTING_LIMIT_EXCEEDED(
      "하루 포스팅 개수 제한을 초과하였습니다.", HttpStatus.TOO_MANY_REQUESTS),
  INVALID_MEETING_DATE_TIME(
      "유효한 모임 시간이 아닙니다.", HttpStatus.BAD_REQUEST);

  private final String message;
  private final HttpStatus status;
}
