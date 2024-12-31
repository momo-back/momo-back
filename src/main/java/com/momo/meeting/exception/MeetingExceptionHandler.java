package com.momo.meeting.exception;

import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class MeetingExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<MeetingErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex
  ) {
    List<MeetingErrorResponse.FieldError> errors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(error -> new MeetingErrorResponse.FieldError(
            error.getField(),
            error.getDefaultMessage(),
            error.getRejectedValue()))
        .collect(Collectors.toList());

    log.info("================== 필드 에러 ==================");
    MeetingErrorResponse errorResponse = new MeetingErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        "올바르지 않은 값이 있습니다.",
        errors
    );
    return ResponseEntity
        .badRequest()
        .body(errorResponse);
  }
}
