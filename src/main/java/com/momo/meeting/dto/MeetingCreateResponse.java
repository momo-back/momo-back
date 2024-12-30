package com.momo.meeting.dto;

import com.momo.meeting.constant.FoodCategory;
import com.momo.meeting.constant.MeetingStatus;
import com.momo.meeting.entity.Meeting;
import com.momo.user.entity.User;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MeetingCreateResponse {

  private Long id;
  private String title;
  private LocalDateTime meetingDateTime;
  private Integer approvedCount;
  private Integer maxCount;
  private Long locationId;
  private Set<FoodCategory> categories;
  private String content;
  private String thumbnailUrl;
  private MeetingStatus meetingStatus;

  public static MeetingCreateResponse from(Meeting meeting) {
    return MeetingCreateResponse.builder()
        .id(meeting.getId())
        .title(meeting.getTitle())
        .meetingDateTime(meeting.getMeetingDateTime())
        .approvedCount(meeting.getApprovedCount())
        .maxCount(meeting.getMaxCount())
        .locationId(meeting.getLocationId())
        .categories(meeting.getCategories())
        .content(meeting.getContent())
        .thumbnailUrl(meeting.getThumbnailUrl())
        .meetingStatus(meeting.getMeetingStatus())
        .build();
  }
}
