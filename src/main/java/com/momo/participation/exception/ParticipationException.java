package com.momo.participation.exception;

import lombok.Getter;

@Getter
public class ParticipationException extends RuntimeException {

  private final ParticipationErrorCode participationErrorCode;

  public ParticipationException(ParticipationErrorCode participationErrorCode) {
    super(participationErrorCode.getMessage());
    this.participationErrorCode = participationErrorCode;
  }
}
