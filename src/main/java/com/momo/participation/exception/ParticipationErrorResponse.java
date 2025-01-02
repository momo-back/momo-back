package com.momo.participation.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ParticipationErrorResponse {

  private final String message;
}
