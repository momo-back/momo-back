package com.momo.meeting.dto.createdMeeting;

import com.momo.meeting.projection.CreatedMeetingProjection;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreatedMeetingsResponse {

  private List<CreatedMeetingDto> createdMeetingDtos;
  private Long lastId;
  private boolean hasNext;


  public static CreatedMeetingsResponse of(
      List<CreatedMeetingProjection> createdMeetingProjections, int pageSize
  ) {
    List<CreatedMeetingDto> createdMeetingDtos =
        CreatedMeetingDto.convertToCreatedMeetingDto(createdMeetingProjections);

    boolean hasNext = createdMeetingDtos.size() > pageSize;

    createdMeetingDtos = hasNext ?
        createdMeetingDtos.subList(0, pageSize) :
        createdMeetingDtos.subList(0, createdMeetingDtos.size());

    Long lastId =
        hasNext ? createdMeetingDtos.get(createdMeetingDtos.size() - 1).getMeetingId() : null;

    return CreatedMeetingsResponse.builder()
        .lastId(lastId)
        .createdMeetingDtos(createdMeetingDtos)
        .hasNext(hasNext)
        .build();
  }
}
