package com.momo.user.exception;

import lombok.Getter;

@Getter
public class UserException extends RuntimeException {

  private final UserErrorCode userErrorCode;

  public UserException(UserErrorCode errorCode) {
    super(errorCode.getMessage());
    this.userErrorCode = errorCode;
  }
}