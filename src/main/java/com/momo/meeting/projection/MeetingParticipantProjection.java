package com.momo.meeting.projection;

import com.momo.participation.constant.ParticipationStatus;

public interface MeetingParticipantProjection {

  Long getUserId();

  String getNickname();

  String getProfileImage();

  ParticipationStatus getParticipationStatus();
}
