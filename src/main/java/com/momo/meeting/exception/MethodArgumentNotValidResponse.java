package com.momo.meeting.exception;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MethodArgumentNotValidResponse {

  private final int status;
  private final String message;
  private final List<FieldError> errors;

  @Getter
  @AllArgsConstructor
  public static class FieldError {

    private String field;
    private String message;
    private Object rejectedValue;
  }
}
