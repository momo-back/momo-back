package com.momo.participation.dto;

import com.momo.meeting.constant.FoodCategory;
import com.momo.participation.constant.ParticipationStatus;
import com.momo.participation.projection.AppliedMeetingsProjection;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AppliedMeetingDto {

  private Long id;
  private Long meetingId;
  private Long userId;
  private ParticipationStatus participationStatus;
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

  public static AppliedMeetingDto from(AppliedMeetingsProjection appliedMeeting) {
    Set<String> foodCategories = FoodCategory.convertToFoodCategories(appliedMeeting.getCategory());

    return AppliedMeetingDto.builder()
        .id(appliedMeeting.getId())
        .meetingId(appliedMeeting.getMeetingId())
        .userId(appliedMeeting.getUserId())
        .participationStatus(appliedMeeting.getParticipationStatus())
        .title(appliedMeeting.getTitle())
        .locationId(appliedMeeting.getLocationId())
        .latitude(appliedMeeting.getLatitude())
        .longitude(appliedMeeting.getLongitude())
        .address(appliedMeeting.getAddress())
        .meetingDateTime(appliedMeeting.getMeetingDateTime().truncatedTo(ChronoUnit.MINUTES))
        .maxCount(appliedMeeting.getMaxCount())
        .approvedCount(appliedMeeting.getApprovedCount())
        .category(foodCategories)
        .content(appliedMeeting.getContent())
        .thumbnailUrl(appliedMeeting.getThumbnailUrl())
        .build();
  }
}
