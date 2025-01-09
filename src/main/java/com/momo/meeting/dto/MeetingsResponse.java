package com.momo.meeting.dto;

import com.momo.meeting.projection.MeetingToMeetingDtoProjection;
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
      List<MeetingToMeetingDtoProjection> meetingProjections,
      int pageSize
  ) {
    List<MeetingDto> meetingDtos = MeetingDto.convertToMeetingDtos(meetingProjections);
    boolean hasNext = meetingDtos.size() > pageSize;

    meetingDtos =
        hasNext ? meetingDtos.subList(0, pageSize) : meetingDtos.subList(0, meetingDtos.size());

    MeetingCursor cursor = MeetingCursor.createCursor(meetingDtos);
    MeetingCursor nextCursor = hasNext ? cursor : null;

    return MeetingsResponse.builder()
        .meetings(meetingDtos)
        .hasNext(hasNext)
        .cursor(nextCursor)
        .build();
  }
}
