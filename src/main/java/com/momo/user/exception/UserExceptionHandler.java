package com.momo.user.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class UserExceptionHandler {

  @ExceptionHandler(UserException.class)
  public ResponseEntity<UserErrorResponse> handleCustomException(UserException e) {
    log.error("User Exception: {}", e.getMessage());
    return createErrorResponse(e.getUserErrorCode());
  }

  private ResponseEntity<UserErrorResponse> createErrorResponse(UserErrorCode errorCode) {
    return ResponseEntity
        .status(errorCode.getStatus())
        .body(new UserErrorResponse(errorCode.getMessage()));
  }
}
