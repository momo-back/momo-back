package com.momo.join.exception;

import com.momo.common.exception.ErrorCode;
import com.momo.common.exception.ErrorResponse;
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
        .status(ErrorCode.VALIDATION_ERROR.getStatus())
        .body(new ErrorResponse(
            ErrorCode.VALIDATION_ERROR,
            e.getField(),
            e.getMessage()
        ));
  }

  @ExceptionHandler(DuplicateException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateException(DuplicateException e) {
    log.error("Duplicate Exception: {}", e.getMessage());
    return ResponseEntity
        .status(ErrorCode.DUPLICATE_ERROR.getStatus())
        .body(new ErrorResponse(
            ErrorCode.DUPLICATE_ERROR,
            e.getField(),
            null
        ));
  }

  @ExceptionHandler(Exception.class) // 기타 모든 예외 처리
  public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
    log.error("Unexpected Exception: {}", e.getMessage());
    return ResponseEntity
        .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
        .body(new ErrorResponse(
            ErrorCode.INTERNAL_SERVER_ERROR
        ));
  }
}
