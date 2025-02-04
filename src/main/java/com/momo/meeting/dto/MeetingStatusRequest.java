package com.momo.meeting.dto;

import com.momo.meeting.constant.MeetingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingStatusRequest {

  MeetingStatus meetingStatus;
}
