package com.momo.notification.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE) // 높은 우선순위로 설정
@RestControllerAdvice(basePackages = "com.momo.notification")
public class NotificationExceptionHandler {

  @ExceptionHandler(NotificationException.class)
  public ResponseEntity<NotificationErrorResponse> handlerNotificationException(
      NotificationException e
  ) {
    log.error("Notification Exception: {}", e.getNotificationErrorCode().getMessage());
    return createErrorResponse(e.getNotificationErrorCode());
  }

  private ResponseEntity<NotificationErrorResponse> createErrorResponse(
      NotificationErrorCode errorCode) {
    return ResponseEntity
        .status(errorCode.getStatus())
        .body(new NotificationErrorResponse(errorCode.getMessage()));
  }
}
