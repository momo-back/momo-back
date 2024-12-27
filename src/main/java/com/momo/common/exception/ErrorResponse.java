package com.momo.common.exception;

public class ErrorResponse {
  private final String message;
  private final String code;
  private final String field;
  private final String details;

  // ErrorCode를 활용한 기본 생성자
  public ErrorResponse(ErrorCode errorCode) {
    this.message = errorCode.getMessage();
    this.code = errorCode.name();
    this.field = null;
    this.details = null;
  }

  // ErrorCode와 추가 정보를 활용한 생성자
  public ErrorResponse(ErrorCode errorCode, String field, String details) {
    this.message = errorCode.getMessage();
    this.code = errorCode.name();
    this.field = field;
    this.details = details;
  }

  // 새로 추가된 생성자 (String, int)
  public ErrorResponse(String message, int status) {
    this.message = message;
    this.code = "ERROR"; // 기본 에러 코드
    this.field = null;
    this.details = null;
  }

  // Getter 메서드들
  public String getMessage() {
    return message;
  }

  public String getCode() {
    return code;
  }

  public String getField() {
    return field;
  }

  public String getDetails() {
    return details;
  }
}