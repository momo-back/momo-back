package com.momo.common.exception;

public enum ErrorCode {
  INVALID_VERIFICATION_TOKEN("유효하지 않은 인증 토큰입니다.", 400),
  VALIDATION_ERROR("잘못된 요청입니다", 400),
  DUPLICATE_ERROR("이미 사용 중인 값입니다", 409),
  INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다", 500),
  USER_NOT_FOUND("사용자를 찾을 수 없습니다", 404),
  NOT_EXISTS_PROFILE("프로필 생성을 완료해 주세요.", 403),
  INVALID_KAKAO_RESPONSE("잘못된 Kakao API 응답입니다.", 400),
  INVALID_REQUEST("잘못된 요청입니다.", 400),
  INVALID_TOKEN("유효하지 않은 토큰입니다.", 400),
  EMAIL_SEND_FAILED("메일 발송을 실패했습니다.", 400),
  PROFILE_NOT_FOUND("프로필을 찾을 수 없습니다.", 400),
  KAKAO_UNLINK_FAILED("카카오 계정 연동해제를 실패했습니다.", 400),
  OPTIMISTIC_LOCKING_FAILURE(
      "이미 다른 처리가 진행되었습니다. 새로고침 후 다시 시도해 주세요.", 400),
  INVALID_VERIFICATION_CODE("유효하지않은 토큰입니다.",400);

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