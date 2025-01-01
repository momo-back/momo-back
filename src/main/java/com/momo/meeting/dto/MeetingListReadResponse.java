package com.momo.meeting.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MeetingListReadResponse {

  private List<MeetingDto> meetings;
  private boolean hasNext;
  private MeetingCursor cursor;

  public static MeetingListReadResponse of(
      List<MeetingDto> meetings,
      MeetingCursor meetingCursor,
      int pageSize
  ) {
    boolean hasNext = meetings.size() > pageSize;
    MeetingCursor nextCursor = hasNext ? meetingCursor : null;

    return MeetingListReadResponse.builder()
        .meetings(meetings)
        .hasNext(hasNext)
        .cursor(nextCursor)
        .build();
  }
}
