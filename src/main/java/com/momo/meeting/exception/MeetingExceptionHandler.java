package com.momo.meeting.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE) // 높은 우선순위로 설정
@RestControllerAdvice(basePackages = {"com.momo.meeting", "com.momo.participation"})
public class MeetingExceptionHandler {

  @ExceptionHandler(MeetingException.class)
  public ResponseEntity<MeetingErrorResponse> handleMeetingException(MeetingException e) {
    log.warn("Meeting Exception: {}", e.getMessage());
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

    log.warn("================== 필드 에러 ==================");
    MethodArgumentNotValidResponse errorResponse = new MethodArgumentNotValidResponse(
        HttpStatus.BAD_REQUEST.value(),
        "올바르지 않은 값이 있습니다.",
        errors
    );
    return ResponseEntity
        .badRequest()
        .body(errorResponse);
  }

  /**
   * enum 타입이 일치하지 않을 때 발생하는 예외 처리
   * HttpMessageNotReadableException 중에서 enum 관련 예외만 처리
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  protected ResponseEntity<String> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException e
  ) {
    Throwable cause = e.getCause();

    if (cause instanceof InvalidFormatException) {
      InvalidFormatException invalidFormatException = (InvalidFormatException) cause;

      // enum 타입 불일치 예외인 경우
      if (invalidFormatException.getTargetType().isEnum()) {
        String enumClassName = invalidFormatException.getTargetType().getSimpleName();
        String invalidValue = invalidFormatException.getValue().toString();
        String message = String.format(
            "[%s]에 [%s]는 존재하지 않는 값입니다.",
            enumClassName,
            invalidValue
        );

        log.warn("Enum Type Mismatch: {}", message);
        return ResponseEntity
            .badRequest()
            .body(message);
      }
    }
    // enum 타입이 아닌 다른 JSON 파싱 예외
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body("잘못된 요청입니다.");
  }

  private ResponseEntity<MeetingErrorResponse> createErrorResponse(MeetingErrorCode errorCode) {
    return ResponseEntity
        .status(errorCode.getStatus())
        .body(new MeetingErrorResponse(errorCode.getMessage()));
  }
}
