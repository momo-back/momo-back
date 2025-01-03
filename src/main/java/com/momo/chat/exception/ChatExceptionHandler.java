package com.momo.chat.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE) // 높은 우선순위로 설정
@RestControllerAdvice(basePackages = "com.momo.chat")
public class ChatExceptionHandler {

  @ExceptionHandler(ChatException.class)
  public ResponseEntity<ChatErrorResponse> handleChatException(ChatException e) {
    log.error("Chat Exception: {}", e.getMessage());
    return ResponseEntity
        .status(e.getChatErrorCode().getStatus())
        .body(new ChatErrorResponse(e.getChatErrorCode().getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ChatErrorResponse> handleGenericException(Exception e) {
    log.error("Unexpected error: {}", e.getMessage());
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ChatErrorResponse("서버 내부 오류가 발생했습니다."));
  }
}
