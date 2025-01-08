package com.momo.participation.dto;

import com.momo.participation.projection.AppliedMeetingsProjection;
import java.util.ArrayList;
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
      List<AppliedMeetingsProjection> appliedMeetingProjections, int pageSize
  ) {
    boolean hasNext = appliedMeetingProjections.size() > pageSize;

    appliedMeetingProjections = hasNext ?
        appliedMeetingProjections.subList(0, pageSize) :
        appliedMeetingProjections.subList(0, appliedMeetingProjections.size());

    return AppliedMeetingsResponse.builder()
        .appliedMeetings(convertToAppliedMeetingDtos(appliedMeetingProjections))
        .lastId(getLastId(appliedMeetingProjections))
        .hasNext(hasNext)
        .build();
  }

  private static List<AppliedMeetingDto> convertToAppliedMeetingDtos(
      List<AppliedMeetingsProjection> appliedMeetingProjections
  ) {
    List<AppliedMeetingDto> appliedMeetingDtos = new ArrayList<>();

    for (AppliedMeetingsProjection appliedMeetingProjection : appliedMeetingProjections) {
      AppliedMeetingDto appliedMeeting = AppliedMeetingDto.from(appliedMeetingProjection);
      appliedMeetingDtos.add(appliedMeeting);
    }
    return appliedMeetingDtos;
  }

  private static Long getLastId(List<AppliedMeetingsProjection> appliedMeetingProjections) {
    if (appliedMeetingProjections.isEmpty()) {
      return null;
    }
    return appliedMeetingProjections.get(appliedMeetingProjections.size() - 1).getId();
  }
}
