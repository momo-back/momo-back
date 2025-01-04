package com.momo.participation.service;

import com.momo.meeting.entity.Meeting;
import com.momo.notification.constant.NotificationType;
import com.momo.notification.service.NotificationService;
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

  private static final String PARTICIPATION_NOTIFICATION_MESSAGE =
      "님이 모임 참여를 신청했습니다.";

  private final ParticipationValidation participationValidation;
  private final ParticipationRepository participationRepository;
  private final NotificationService notificationService;

  @Transactional
  public Long createParticipation(User user, Long meetingId) {
    Meeting meeting = participationValidation.validateForParticipate(user.getId(), meetingId);

    Participation participation = createMeetingParticipant(user, meeting);
    Participation saved = participationRepository.save(participation);

    // 모임 주최자에게 새로운 참여 알림 발송
    notificationService.sendNotification(
        meeting.getUser(),
        user.getNickname() + PARTICIPATION_NOTIFICATION_MESSAGE,
        NotificationType.NEW_PARTICIPATION_REQUEST
    );

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
