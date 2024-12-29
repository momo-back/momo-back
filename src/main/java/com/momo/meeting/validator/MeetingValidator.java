package com.momo.meeting.validator;

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
  //private final UserValidator userValidator;

  public void validateForMeetingCreation(Long userId, LocalDateTime meetingDateTime) {
    // TODO: 일일 모임 생성 제한 검증 Redis 고려
    validateDailyPostLimit(userId);
    validateMeetingDateTime(meetingDateTime);
    // TODO: 프로필 생성됐는지 검증 필요
    //userValidator.existsById(userId); // TODO: merge 후 변경
    if (!userRepository.existsById(userId)) {
      throw new RuntimeException();
    }
  }

  private void validateDailyPostLimit(Long userId) {
    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);

    int todayPostCount = meetingRepository.countByUser_IdAndCreatedAtBetween(userId, startOfDay,
        endOfDay);

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
