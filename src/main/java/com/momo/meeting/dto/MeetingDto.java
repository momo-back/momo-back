package com.momo.meeting.dto;

import com.momo.meeting.constant.FoodCategory;
import com.momo.meeting.projection.MeetingToMeetingDtoProjection;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
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
  private Long authorId;
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
  private String thumbnail;
  private Double distance;

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

  public static MeetingDto from(MeetingToMeetingDtoProjection meetingProjection) {
    Set<String> foodCategories = FoodCategory.convertToFoodCategories(meetingProjection.getCategory());
    return MeetingDto.builder()
        .id(meetingProjection.getId())
        .authorId(meetingProjection.getAuthorId())
        .title(meetingProjection.getTitle())
        .locationId(meetingProjection.getLocationId())
        .latitude(meetingProjection.getLatitude())
        .longitude(meetingProjection.getLongitude())
        .address(meetingProjection.getAddress())
        .meetingDateTime(meetingProjection.getMeetingDateTime().truncatedTo(ChronoUnit.MINUTES))
        .maxCount(meetingProjection.getMaxCount())
        .approvedCount(meetingProjection.getApprovedCount())
        .category(foodCategories)
        .content(meetingProjection.getContent())
        .thumbnail(meetingProjection.getThumbnailUrl())
        .distance(meetingProjection.getDistance())
        .build();
  }
}
