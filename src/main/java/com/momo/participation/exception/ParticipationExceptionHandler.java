package com.momo.participation.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE) // 높은 우선순위로 설정
@RestControllerAdvice(basePackages = "com.momo.participation")
public class ParticipationExceptionHandler {

  @ExceptionHandler(ParticipationException.class)
  public ResponseEntity<ParticipationErrorResponse> handleParticipationException(
      ParticipationException e
  ) {
    log.error("Participation Exception: {}", e.getParticipationErrorCode().getMessage());
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
