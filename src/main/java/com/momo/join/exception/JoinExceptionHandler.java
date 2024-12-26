package com.momo.join.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class JoinExceptionHandler {

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(ValidationException e) {
    log.error("Validation Exception: {}", e.getMessage());
    return ResponseEntity
        .badRequest()
        .body(new ErrorResponse(
            "잘못된 요청입니다",
            "VALIDATION_ERROR",
            e.getField(),
            e.getMessage()
        ));
  }

  @ExceptionHandler(DuplicateException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateException(DuplicateException e) {
    log.error("Duplicate Exception: {}", e.getMessage());
    return ResponseEntity
        .status(409)
        .body(new ErrorResponse(
            "이미 사용 중인 " + e.getField() + "입니다",
            "DUPLICATE_ERROR",
            e.getField(),
            null
        ));
  }

  @ExceptionHandler(Exception.class) // 기타 모든 예외 처리
  public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
    log.error("Unexpected Exception: {}", e.getMessage());
    return ResponseEntity
        .status(500)
        .body(new ErrorResponse(
            "서버 내부 오류가 발생했습니다",
            "INTERNAL_SERVER_ERROR"
        ));
  }
}
