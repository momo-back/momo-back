package com.momo.infrastructure.s3.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class FileExceptionHandler {

  @ExceptionHandler(S3Exception.class)
  public ResponseEntity<ErrorResponse> handleS3Exception(S3Exception e) {
    log.error("File Exception: {}", e.getMessage());
    return createErrorResponse(e.getS3ErrorCode());
  }

  private ResponseEntity<ErrorResponse> createErrorResponse(S3ErrorCode errorCode) {
    return ResponseEntity
        .status(errorCode.getStatus())
        .body(new ErrorResponse(errorCode.getMessage()));
  }
}
