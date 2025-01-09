package com.momo.meeting.dto;

import java.time.LocalDateTime;
import java.util.List;
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

  public static MeetingCursor createCursor(List<MeetingDto> meetingDtos) {
    if (meetingDtos.isEmpty()) {
      return null;
    }
    MeetingDto lastProjection = meetingDtos.get(meetingDtos.size() - 1);

    return MeetingCursor.of(
        lastProjection.getId(),
        lastProjection.getDistance(),
        lastProjection.getMeetingDateTime()
    );
  }
}
