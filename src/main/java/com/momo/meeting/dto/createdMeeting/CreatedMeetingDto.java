package com.momo.meeting.dto.createdMeeting;

import com.momo.meeting.constant.FoodCategory;
import com.momo.meeting.constant.MeetingStatus;
import com.momo.meeting.projection.CreatedMeetingProjection;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreatedMeetingDto {

  private Long id;
  private Long meetingId;
  private MeetingStatus meetingStatus;
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

  public static List<CreatedMeetingDto> convertToCreatedMeetingDto(
      List<CreatedMeetingProjection> createdMeetingProjections
  ) {
    List<CreatedMeetingDto> createdMeetingDtos = new ArrayList<>();

    for (CreatedMeetingProjection createdMeetingProjection : createdMeetingProjections) {
      CreatedMeetingDto createdMeetingDto = CreatedMeetingDto.from(createdMeetingProjection);
      createdMeetingDtos.add(createdMeetingDto);
    }
    return createdMeetingDtos;
  }

  public static CreatedMeetingDto from(CreatedMeetingProjection createdMeetingProjection) {
    Set<String> foodCategories = FoodCategory
        .convertToFoodCategories(createdMeetingProjection.getCategory());

    return CreatedMeetingDto.builder()
        .id(createdMeetingProjection.getId())
        .meetingId(createdMeetingProjection.getMeetingId())
        .meetingStatus(createdMeetingProjection.getMeetingStatus())
        .title(createdMeetingProjection.getTitle())
        .locationId(createdMeetingProjection.getLocationId())
        .latitude(createdMeetingProjection.getLatitude())
        .longitude(createdMeetingProjection.getLongitude())
        .address(createdMeetingProjection.getAddress())
        .meetingDateTime(
            createdMeetingProjection.getMeetingDateTime().truncatedTo(ChronoUnit.MINUTES))
        .maxCount(createdMeetingProjection.getMaxCount())
        .approvedCount(createdMeetingProjection.getApprovedCount())
        .category(foodCategories)
        .content(createdMeetingProjection.getContent())
        .thumbnailUrl(createdMeetingProjection.getThumbnailUrl())
        .build();
  }
}
