package com.momo.profile.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ProfileErrorResponse {

  private final String message;
}
