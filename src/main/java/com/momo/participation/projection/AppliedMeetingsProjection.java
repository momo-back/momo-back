package com.momo.participation.projection;

import com.momo.participation.constant.ParticipationStatus;
import java.time.LocalDateTime;

public interface AppliedMeetingsProjection {

  Long getId();

  Long getMeetingId();

  Long getUserId();

  ParticipationStatus getParticipationStatus();

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

  String getThumbnailUrl();
}
