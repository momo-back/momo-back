package com.momo.participation.projection;

import com.momo.participation.constant.ParticipationStatus;
import java.time.LocalDateTime;

public interface AppliedMeetingProjection {

  Long getId();

  Long getMeetingId();

  Long getAuthorId();

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
