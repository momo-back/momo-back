package com.momo.meeting.projection;

import com.momo.participation.constant.ParticipationStatus;

public interface MeetingParticipantProjection {

  Long getUserId();

  Long getParticipationId();

  String getNickname();

  String getProfileImage();

  ParticipationStatus getParticipationStatus();
}
