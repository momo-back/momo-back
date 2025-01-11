package com.momo.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
    ErrorCode errorCode = ex.getErrorCode();
    ErrorResponse response = new ErrorResponse(errorCode.getMessage(), errorCode.getStatus());
    return ResponseEntity.status(errorCode.getStatus()).body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
    ErrorResponse response = new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR);
    return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus()).body(response);
  }

  @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
  public ResponseEntity<ErrorResponse> handleObjectOptimisticLockingFailureException(
      ObjectOptimisticLockingFailureException e
  ) {
    log.error("Optimistic Locking 실패", e);
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ErrorResponse(ErrorCode.OPTIMISTIC_LOCKING_FAILURE));
  }
}
