package com.momo.meeting.exception;

import lombok.Getter;

@Getter
public class MeetingException extends RuntimeException {

  private final MeetingErrorCode meetingErrorCode;

  public MeetingException(MeetingErrorCode meetingErrorCode) {
    super(meetingErrorCode.getMessage());
    this.meetingErrorCode = meetingErrorCode;
  }
}
