package com.momo.participation.service;

import com.momo.meeting.entity.Meeting;
import com.momo.meeting.exception.MeetingErrorCode;
import com.momo.meeting.exception.MeetingException;
import com.momo.meeting.repository.MeetingRepository;
import com.momo.participation.constant.ParticipationStatus;
import com.momo.participation.entity.Participation;
import com.momo.participation.repository.ParticipationRepository;
import com.momo.participation.validation.ParticipationValidation;
import com.momo.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParticipationService {

  private final ParticipationValidation participationValidation;
  private final ParticipationRepository participationRepository;

  @Transactional
  public Long createParticipation(User user, Long meetingId) {
    Meeting meeting = participationValidation.validateForParticipate(user.getId(), meetingId);

    Participation participation = createMeetingParticipant(user, meeting);
    Participation saved = participationRepository.save(participation);

    return saved.getId();
  }

  private Participation createMeetingParticipant(User user, Meeting meeting) {
    return Participation.builder()
        .user(user)
        .meeting(meeting)
        .participationStatus(ParticipationStatus.PENDING)
        .build();
  }
}
