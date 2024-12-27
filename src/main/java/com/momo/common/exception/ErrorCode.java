package com.momo.common.exception;

public enum ErrorCode {
  INVALID_VERIFICATION_TOKEN("유효하지 않은 인증 토큰입니다.", 400),
  VALIDATION_ERROR("잘못된 요청입니다", 400),
  DUPLICATE_ERROR("이미 사용 중인 값입니다", 409),
  INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다", 500);

  private final String message;
  private final int status;

  ErrorCode(String message, int status) {
    this.message = message;
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public int getStatus() {
    return status;
  }
}