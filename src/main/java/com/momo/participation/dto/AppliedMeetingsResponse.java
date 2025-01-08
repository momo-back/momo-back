package com.momo.participation.dto;

import com.momo.participation.projection.AppliedMeetingProjection;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AppliedMeetingsResponse {

  private List<AppliedMeetingDto> appliedMeetings;
  private Long lastId;
  private boolean hasNext;


  public static AppliedMeetingsResponse of(
      List<AppliedMeetingProjection> appliedMeetingProjections, int pageSize
  ) {
    List<AppliedMeetingDto> appliedMeetingDtos =
        AppliedMeetingDto.convertToAppliedMeetingDtos(appliedMeetingProjections);

    boolean hasNext = appliedMeetingDtos.size() > pageSize;

    appliedMeetingDtos = hasNext ?
        appliedMeetingDtos.subList(0, pageSize) :
        appliedMeetingDtos.subList(0, appliedMeetingDtos.size());

    Long lastId = hasNext ? appliedMeetingDtos.get(appliedMeetingDtos.size() - 1).getId() : null;

    return AppliedMeetingsResponse.builder()
        .lastId(lastId)
        .appliedMeetings(appliedMeetingDtos)
        .hasNext(hasNext)
        .build();
  }
}
