package com.momo.auth.join.exception;

import lombok.Getter;

@Getter
public class DuplicateException extends RuntimeException {

  private final String field;

  public DuplicateException(String field, String message) {
    super(message);
    this.field = field;
  }
}
