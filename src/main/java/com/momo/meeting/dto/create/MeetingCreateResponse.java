package com.momo.meeting.dto.create;

import com.momo.meeting.constant.FoodCategory;
import com.momo.meeting.constant.MeetingStatus;
import com.momo.meeting.entity.Meeting;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MeetingCreateResponse {

  private Long id;
  private String title;
  private Long locationId;
  private Double latitude;
  private Double longitude;
  private String address;
  private LocalDateTime meetingDateTime;
  private Integer approvedCount;
  private Integer maxCount;
  private Set<FoodCategory> categories;
  private String content;
  private String thumbnailUrl;
  private MeetingStatus meetingStatus;

  public static MeetingCreateResponse from(Meeting meeting) {
    return MeetingCreateResponse.builder()
        .id(meeting.getId())
        .title(meeting.getTitle())
        .locationId(meeting.getLocationId())
        .latitude(meeting.getLatitude())
        .longitude(meeting.getLongitude())
        .address(meeting.getAddress())
        .meetingDateTime(meeting.getMeetingDateTime())
        .approvedCount(meeting.getApprovedCount())
        .maxCount(meeting.getMaxCount())
        .categories(meeting.getCategory())
        .content(meeting.getContent())
        .thumbnailUrl(meeting.getThumbnailUrl())
        .meetingStatus(meeting.getMeetingStatus())
        .build();
  }
}
