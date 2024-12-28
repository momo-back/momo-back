package com.momo.meeting.service;

import com.momo.meeting.dto.MeetingCreateRequest;
import com.momo.meeting.dto.MeetingCreateResponse;
import com.momo.meeting.persist.entity.Meeting;
import com.momo.meeting.persist.repository.MeetingRepository;
import com.momo.meeting.validator.MeetingValidator;
import com.momo.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MeetingService {

  private final MeetingRepository meetingRepository;
  private final MeetingValidator meetingValidator;

  public MeetingCreateResponse createMeeting(User user, MeetingCreateRequest request) {
    meetingValidator.validateForMeetingCreation(user.getId(), request.getMeetingDateTime());

    Meeting meeting = request.toEntity(request, user);
    Meeting saved = meetingRepository.save(meeting);

    return MeetingCreateResponse.from(saved);
  }
}
