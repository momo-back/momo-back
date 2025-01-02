package com.momo.participation.validation;

import com.momo.meeting.entity.Meeting;
import com.momo.meeting.exception.MeetingErrorCode;
import com.momo.meeting.exception.MeetingException;
import com.momo.meeting.repository.MeetingRepository;
import com.momo.participation.exception.ParticipationErrorCode;
import com.momo.participation.exception.ParticipationException;
import com.momo.participation.repository.ParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParticipationValidation {

  private final MeetingRepository meetingRepository;
  private final ParticipationRepository participationRepository;

  public Meeting validateForParticipate(Long userId, Long meetingId) {
    Meeting meeting = meetingRepository.findById(meetingId)
        .orElseThrow(() -> new MeetingException(MeetingErrorCode.MEETING_NOT_FOUND));

    if (!meeting.getMeetingStatus().isParticipate()) { // 참여 가능한 상태의 모임글인지 검증
      throw new ParticipationException(ParticipationErrorCode.INVALID_MEETING_STATUS);
    }

    if (userId.equals(meeting.getUser().getId())) { // 본인이 작성한 모임글인지 검증
      throw new ParticipationException(ParticipationErrorCode.PARTICIPATE_SELF_MEETING_NOT_ALLOW);
    }

    // 이미 참여 신청한 모임인지
    if (participationRepository.findByUser_IdAndMeeting_Id(userId, meeting.getId())) {
      throw new ParticipationException(ParticipationErrorCode.ALREADY_PARTICIPATE_MEETING);
    }
    return meeting;
  }
}
