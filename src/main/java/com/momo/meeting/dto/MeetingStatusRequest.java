package com.momo.meeting.dto;

import com.momo.meeting.constant.MeetingStatus;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MeetingStatusRequest {

  MeetingStatus meetingStatus;
}
