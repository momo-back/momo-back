package com.momo.meeting.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MeetingCursor {

  private final Long id;
  private final Double distance;
  private final LocalDateTime meetingDateTime;

  public static MeetingCursor of(Long id, Double distance, LocalDateTime meetingDateTime) {
    return MeetingCursor.builder()
        .id(id)
        .distance(distance)
        .meetingDateTime(meetingDateTime)
        .build();
  }
}
