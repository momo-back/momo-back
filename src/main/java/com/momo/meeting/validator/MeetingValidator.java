package com.momo.meeting.validator;

import com.momo.common.exception.CustomException;
import com.momo.common.exception.ErrorCode;
import com.momo.meeting.exception.MeetingErrorCode;
import com.momo.meeting.exception.MeetingException;
import com.momo.meeting.repository.MeetingRepository;
import com.momo.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MeetingValidator {

  private final MeetingRepository meetingRepository;
  private final UserRepository userRepository;

  public void validateForMeetingCreation(Long userId, LocalDateTime meetingDateTime) {
    validateDailyPostLimit(userId);
    validateMeetingDateTime(meetingDateTime);
    validateUser(userId);
  }

  private void validateDailyPostLimit(Long userId) {
    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);

    int todayPostCount = meetingRepository.countByUser_IdAndCreatedAtBetween(
        userId, startOfDay, endOfDay
    );
    if (todayPostCount >= 10) {
      throw new MeetingException(MeetingErrorCode.DAILY_POSTING_LIMIT_EXCEEDED);
    }
  }

  private void validateMeetingDateTime(LocalDateTime meetingDateTime) {
    if (meetingDateTime.isBefore(LocalDateTime.now())) {
      throw new MeetingException(MeetingErrorCode.INVALID_MEETING_DATE_TIME);
    }
  }

  private void validateUser(Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }
  }
}
