package com.momo.participation.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ParticipationErrorCode {

  PARTICIPATION_NOT_FOUND("신청을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

  INVALID_MEETING_STATUS("신청할 수 없는 모임 상태입니다.", HttpStatus.BAD_REQUEST),

  INVALID_PARTICIPATION_STATUS(
      "유효하지 않은 참여 상태입니다.", HttpStatus.BAD_REQUEST),

  PARTICIPATE_SELF_MEETING_NOT_ALLOW(
      "본인이 개설한 모임에는 참여 신청을 할 수 없습니다.", HttpStatus.FORBIDDEN),

  NOT_PARTICIPATION_OWNER("해당 참여 신청의 신청자가 아닙니다.", HttpStatus.FORBIDDEN),

  ALREADY_PARTICIPATE_MEETING("이미 참여 신청한 모임입니다.", HttpStatus.CONFLICT);

  private final String message;
  private final HttpStatus status;
}
