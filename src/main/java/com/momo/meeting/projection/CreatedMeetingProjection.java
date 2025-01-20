package com.momo.meeting.projection;

import com.momo.meeting.constant.MeetingStatus;
import java.time.LocalDateTime;

public interface CreatedMeetingProjection {

  Long getUserId();

  Long getMeetingId();

  MeetingStatus getMeetingStatus();

  String getTitle();

  Long getLocationId();

  Double getLatitude();

  Double getLongitude();

  String getAddress();

  LocalDateTime getMeetingDateTime();

  Integer getMaxCount();

  Integer getApprovedCount();

  String getCategory();

  String getContent();

  String getThumbnail();
}
