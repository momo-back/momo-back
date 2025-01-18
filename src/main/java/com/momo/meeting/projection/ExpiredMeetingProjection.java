package com.momo.meeting.projection;

import com.momo.user.entity.User;

public interface ExpiredMeetingProjection {

  Long getMeetingId();

  String getTitle();

  User getAuthor();
}
