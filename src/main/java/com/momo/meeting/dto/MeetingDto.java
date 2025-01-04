package com.momo.meeting.dto;

import com.momo.meeting.constant.FoodCategory;
import com.momo.meeting.exception.MeetingErrorCode;
import com.momo.meeting.exception.MeetingException;
import com.momo.meeting.projection.MeetingToMeetingDtoProjection;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Builder
@ToString
public class MeetingDto {

  private Long id;
  private String title;
  private Long locationId;
  private Double latitude;
  private Double longitude;
  private String address;
  private LocalDateTime meetingDateTime;
  private Integer maxCount;
  private Integer approvedCount;
  private Set<String> category;
  private String content;
  private String thumbnailUrl;

  public static List<MeetingDto> convertToMeetingDtos(
      List<MeetingToMeetingDtoProjection> meetingProjections
  ) {
    List<MeetingDto> meetingDtos = new ArrayList<>();
    for (MeetingToMeetingDtoProjection meetingProjection : meetingProjections) {
      MeetingDto meetingDto = MeetingDto.from(meetingProjection);
      meetingDtos.add(meetingDto);
    }
    return meetingDtos;
  }

  public static MeetingDto from(MeetingToMeetingDtoProjection meeting) {
    Set<String> foodCategories = FoodCategory.convertToFoodCategories(meeting.getCategory());
    return MeetingDto.builder()
        .id(meeting.getId())
        .title(meeting.getTitle())
        .locationId(meeting.getLocationId())
        .latitude(meeting.getLatitude())
        .longitude(meeting.getLongitude())
        .address(meeting.getAddress())
        .meetingDateTime(meeting.getMeetingDateTime().truncatedTo(ChronoUnit.MINUTES))
        .maxCount(meeting.getMaxCount())
        .approvedCount(meeting.getApprovedCount())
        .category(foodCategories)
        .content(meeting.getContent())
        .thumbnailUrl(meeting.getThumbnailUrl())
        .build();
  }
}
