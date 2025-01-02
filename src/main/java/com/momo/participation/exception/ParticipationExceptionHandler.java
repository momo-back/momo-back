package com.momo.participation.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ParticipationExceptionHandler {

  @ExceptionHandler(ParticipationException.class)
  public ResponseEntity<?> handleParticipationException(ParticipationException e) {
    return createErrorResponse(e.getParticipationErrorCode());
  }

  private ResponseEntity<ParticipationErrorResponse> createErrorResponse(
      ParticipationErrorCode errorCode
  ) {
    return ResponseEntity
        .status(errorCode.getStatus())
        .body(new ParticipationErrorResponse(errorCode.getMessage()));
  }
}
