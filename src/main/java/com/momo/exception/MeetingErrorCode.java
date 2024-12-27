package com.momo.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MeetingErrorCode {

  DAILY_POSTING_LIMIT_EXCEEDED(
      "하루 포스팅 개수 제한을 초과하였습니다.", HttpStatus.TOO_MANY_REQUESTS
  );

  private final String message;
  private final HttpStatus status;
}
