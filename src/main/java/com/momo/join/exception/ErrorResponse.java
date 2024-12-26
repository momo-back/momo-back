package com.momo.join.exception;

import lombok.Getter;

@Getter
public class ErrorResponse {

  private final String message; // 전체적인 에러 메시지
  private final String code;    // 에러 코드
  private final String field;   // 필드 이름 (중복 체크 등에서 사용)
  private final String details; // 상세 메시지 (옵션)

  // 기본 생성자
  public ErrorResponse(String message, String code, String field, String details) {
    this.message = message;
    this.code = code;
    this.field = field;
    this.details = details;
  }

  // 간단한 에러 생성자 (필드와 상세 메시지 생략)
  public ErrorResponse(String message, String code) {
    this(message, code, null, null);
  }

  // 필드 관련 에러 생성자 (상세 메시지 생략)
  public ErrorResponse(String message, String code, String field) {
    this(message, code, field, null);
  }
}
