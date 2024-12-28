package com.momo.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode {

  USER_NOT_FOUND("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

  private final String message;
  private final HttpStatus status;
}