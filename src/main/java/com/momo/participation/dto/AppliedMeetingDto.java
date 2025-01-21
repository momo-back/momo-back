package com.momo.participation.dto;

import com.momo.meeting.constant.FoodCategory;
import com.momo.participation.constant.ParticipationStatus;
import com.momo.participation.projection.AppliedMeetingProjection;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AppliedMeetingDto {

  private Long id;
  private Long meetingId;
  private Long authorId;
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
  private String thumbnail;

  public static List<AppliedMeetingDto> convertToAppliedMeetingDtos(
      List<AppliedMeetingProjection> appliedMeetingProjections
  ) {
    List<AppliedMeetingDto> appliedMeetingDtos = new ArrayList<>();

    for (AppliedMeetingProjection appliedMeetingProjection : appliedMeetingProjections) {
      AppliedMeetingDto appliedMeeting = AppliedMeetingDto.from(appliedMeetingProjection);
      appliedMeetingDtos.add(appliedMeeting);
    }
    return appliedMeetingDtos;
  }

  public static AppliedMeetingDto from(AppliedMeetingProjection appliedMeeting) {
    Set<String> foodCategories = FoodCategory.convertToFoodCategories(appliedMeeting.getCategory());

    return AppliedMeetingDto.builder()
        .id(appliedMeeting.getId())
        .meetingId(appliedMeeting.getMeetingId())
        .authorId(appliedMeeting.getAuthorId())
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
        .thumbnail(appliedMeeting.getThumbnail())
        .build();
  }
}
