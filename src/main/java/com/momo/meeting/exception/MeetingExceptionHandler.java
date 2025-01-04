package com.momo.meeting.exception;

import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE) // 높은 우선순위로 설정
@RestControllerAdvice(basePackages = "com.momo.meeting")
public class MeetingExceptionHandler {

  @ExceptionHandler(MeetingException.class)
  public ResponseEntity<MeetingErrorResponse> handleMeetingException(MeetingException e) {
    log.error("Meeting Exception: {}", e.getMessage());
    return createErrorResponse(e.getMeetingErrorCode());
  }

  /**
   * DTO 클래스에서 검증 어노테이션으로 검증 시도 시, 유효하지 않으면 MethodArgumentNotValidException 발생
   *
   * @param e
   * @return
   */

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<MethodArgumentNotValidResponse> handleValidationException(
      MethodArgumentNotValidException e
  ) {
    List<MethodArgumentNotValidResponse.FieldError> errors = e.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(error -> new MethodArgumentNotValidResponse.FieldError(
            error.getField(),
            error.getDefaultMessage(),
            error.getRejectedValue()))
        .collect(Collectors.toList());

    log.info("================== 필드 에러 ==================");
    MethodArgumentNotValidResponse errorResponse = new MethodArgumentNotValidResponse(
        HttpStatus.BAD_REQUEST.value(),
        "올바르지 않은 값이 있습니다.",
        errors
    );
    return ResponseEntity
        .badRequest()
        .body(errorResponse);
  }

  private ResponseEntity<MeetingErrorResponse> createErrorResponse(MeetingErrorCode errorCode) {
    return ResponseEntity
        .status(errorCode.getStatus())
        .body(new MeetingErrorResponse(errorCode.getMessage()));
  }
}
