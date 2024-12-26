package com.momo.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
    ErrorCode errorCode = ex.getErrorCode();
    ErrorResponse response = new ErrorResponse(errorCode.getMessage(), errorCode.getStatus());
    return ResponseEntity.status(errorCode.getStatus()).body(response);
  }

}
