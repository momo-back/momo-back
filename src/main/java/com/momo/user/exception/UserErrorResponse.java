package com.momo.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserErrorResponse {

  private final String message;
}