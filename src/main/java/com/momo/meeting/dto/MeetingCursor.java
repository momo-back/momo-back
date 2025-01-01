package com.momo.meeting.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MeetingCursor {

  private Long id;
  private Double distance;

  public static MeetingCursor of(Long id, Double distance) {
    return MeetingCursor.builder()
        .id(id)
        .distance(distance)
        .build();
  }
}
