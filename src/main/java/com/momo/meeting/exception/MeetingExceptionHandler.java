package com.momo.meeting.exception;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MeetingExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<MeetingErrorResponse> handleValidationExceptions(
      MethodArgumentNotValidException e
  ) {
    Map<String, String> errors = new HashMap<>();
    for (FieldError error : e.getBindingResult().getFieldErrors()) {
      errors.put(error.getField(), error.getDefaultMessage());
    }

    return ResponseEntity
        .badRequest()
        .body(new MeetingErrorResponse(errors));
  }
}
