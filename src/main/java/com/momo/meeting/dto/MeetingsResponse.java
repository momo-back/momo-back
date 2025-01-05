package com.momo.meeting.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MeetingsResponse {

  private List<MeetingDto> meetings;
  private boolean hasNext;
  private MeetingCursor cursor;

  public static MeetingsResponse of(
      List<MeetingDto> meetings,
      MeetingCursor meetingCursor,
      int pageSize
  ) {
    boolean hasNext = meetings.size() > pageSize;
    MeetingCursor nextCursor = hasNext ? meetingCursor : null;

    return MeetingsResponse.builder()
        .meetings(meetings)
        .hasNext(hasNext)
        .cursor(nextCursor)
        .build();
  }
}
