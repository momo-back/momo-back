package com.momo.meeting.exception;

import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MeetingErrorResponse {

  private final Map<String, String> errors;
}
