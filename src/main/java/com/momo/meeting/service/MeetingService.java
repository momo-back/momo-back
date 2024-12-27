package com.momo.meeting.service;

import com.momo.exception.MeetingErrorCode;
import com.momo.exception.MeetingException;
import com.momo.meeting.dto.CreateMeetingRequest;
import com.momo.meeting.persist.entity.Meeting;
import com.momo.meeting.persist.repository.MeetingRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MeetingService {

  private final MeetingRepository meetingRepository;
  private final UserRepository userRepository;

  public Long createMeeting(CreateMeetingRequest request, Long userId) {
    validateDailyPostLimit(userId);

    User author = userRepository.findById()
        .orElseThrow(() -> new UserNotFoundException(userId));

    Meeting meeting = request.toEntity(request);

    return meetingRepository.save(meeting)
  }

  private void validateDailyPostLimit(Long userId) {
    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);

    long todayPostCount = meetingRepository
        .countByUser_IdAndCreatedAtBetween(userId, startOfDay, endOfDay);

    if (todayPostCount >= 10) {
      throw new MeetingException(MeetingErrorCode.DAILY_POSTING_LIMIT_EXCEEDED);
    }
  }
}
