package com.momo.meeting.dto;

import com.momo.meeting.constant.FoodCategory;
import com.momo.meeting.constant.MeetingStatus;
import com.momo.meeting.entity.Meeting;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MeetingResponse {

  private Long id;
  private String title;
  private Long locationId;
  private Double latitude;
  private Double longitude;
  private String address;
  private LocalDateTime meetingDateTime;
  private Integer maxCount;
  private Integer approvedCount;
  private Set<FoodCategory> category;
  private String content;
  private String thumbnail;
  private MeetingStatus meetingStatus;

  public static MeetingResponse from(Meeting meeting) {
    return MeetingResponse.builder()
        .id(meeting.getId())
        .title(meeting.getTitle())
        .locationId(meeting.getLocationId())
        .latitude(meeting.getLatitude())
        .longitude(meeting.getLongitude())
        .address(meeting.getAddress())
        .meetingDateTime(meeting.getMeetingDateTime())
        .maxCount(meeting.getMaxCount())
        .approvedCount(meeting.getApprovedCount())
        .category(meeting.getCategory())
        .content(meeting.getContent())
        .thumbnail(meeting.getThumbnail())
        .meetingStatus(meeting.getMeetingStatus())
        .build();
  }
}
