package com.momo.meeting.service;

import com.momo.exception.MeetingErrorCode;
import com.momo.exception.MeetingException;
import com.momo.meeting.dto.CreateMeetingRequest;
import com.momo.meeting.persist.entity.Meeting;
import com.momo.meeting.persist.repository.MeetingRepository;
import com.momo.mock.MockUser;
import com.momo.mock.MockUserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MeetingService {

  private final MeetingRepository meetingRepository;
  private final MockUserRepository userRepository; // TODO: merge 후 변경

  public Long createMeeting(CreateMeetingRequest request, Long userId) {
    validateMeetingDateTime(request.getMeetingDateTime());
    validateDailyPostLimit(userId);

    // TODO: merge 후 변경
    MockUser user = userRepository.findById(userId)
        .orElseThrow(RuntimeException::new);

    Meeting meeting = request.toEntity(request);

    return meetingRepository.save(meeting).getId();
  }

  private void validateDailyPostLimit(Long userId) {
    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);

    int todayPostCount = meetingRepository
        .countByUser_IdAndCreatedAtBetween(userId, startOfDay, endOfDay);

    if (todayPostCount >= 10) {
      throw new MeetingException(MeetingErrorCode.DAILY_POSTING_LIMIT_EXCEEDED);
    }
  }

  private void validateMeetingDateTime(LocalDateTime meetingDateTime) {
    if (meetingDateTime.isBefore(LocalDateTime.now())) {
      throw new MeetingException(MeetingErrorCode.INVALID_MEETING_DATE_TIME);
    }
  }
}
