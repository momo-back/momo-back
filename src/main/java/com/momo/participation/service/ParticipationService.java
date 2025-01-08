package com.momo.participation.service;

import com.momo.meeting.constant.MeetingStatus;
import com.momo.meeting.entity.Meeting;
import com.momo.meeting.exception.MeetingErrorCode;
import com.momo.meeting.exception.MeetingException;
import com.momo.meeting.repository.MeetingRepository;
import com.momo.notification.constant.NotificationType;
import com.momo.notification.service.NotificationService;
import com.momo.participation.constant.ParticipationStatus;
import com.momo.participation.dto.AppliedMeetingsResponse;
import com.momo.participation.entity.Participation;
import com.momo.participation.exception.ParticipationErrorCode;
import com.momo.participation.exception.ParticipationException;
import com.momo.participation.projection.AppliedMeetingsProjection;
import com.momo.participation.repository.ParticipationRepository;
import com.momo.user.entity.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParticipationService {

  private static final String PARTICIPATION_NOTIFICATION_MESSAGE =
      "님이 모임 참여를 신청했습니다.";

  private final ParticipationRepository participationRepository;
  private final MeetingRepository meetingRepository;
  private final NotificationService notificationService;

  public Long createParticipation(User user, Long meetingId) {
    Meeting meeting = validateForParticipate(user.getId(), meetingId);

    Participation participation = Participation.createMeetingParticipant(user, meeting);
    Participation saved = participationRepository.save(participation);

    // 모임 주최자에게 새로운 참여 알림 발송
    notificationService.sendNotification(
        meeting.getUser(),
        user.getNickname() + PARTICIPATION_NOTIFICATION_MESSAGE,
        NotificationType.NEW_PARTICIPATION_REQUEST
    );
    return saved.getId();
  }

  private Meeting validateForParticipate(Long userId, Long meetingId) {
    Meeting meeting = meetingRepository.findById(meetingId)
        .orElseThrow(() -> new MeetingException(MeetingErrorCode.MEETING_NOT_FOUND));

    if (!meeting.getMeetingStatus().isParticipate()) { // 참여 가능한 상태의 모임글인지 검증
      throw new ParticipationException(ParticipationErrorCode.INVALID_MEETING_STATUS);
    }

    if (userId.equals(meeting.getUser().getId())) { // 본인이 작성한 모임글인지 검증
      throw new ParticipationException(ParticipationErrorCode.PARTICIPATE_SELF_MEETING_NOT_ALLOW);
    }

    // 이미 참여 신청한 모임인지
    if (participationRepository.existsByUser_IdAndMeeting_Id(userId, meeting.getId())) {
      throw new ParticipationException(ParticipationErrorCode.ALREADY_PARTICIPATE_MEETING);
    }
    return meeting;
  }

  public AppliedMeetingsResponse getAppliedMeetings(Long userId, Long lastId, int pageSize) {
    return AppliedMeetingsResponse.of(
        getAppliedMeetingsProjections(userId, lastId, pageSize),
        pageSize
    );
  }

  private List<AppliedMeetingsProjection> getAppliedMeetingsProjections(
      Long userId, Long lastId, int pageSize
  ) {
    return participationRepository.findAppliedMeetingsWithLastId(
        userId,
        lastId,
        pageSize + 1// 다음 페이지 존재 여부를 알기 위해 + 1
    );
  }

  @Transactional
  public void updateParticipationStatus(
      Long id, Long participationId, ParticipationStatus newStatus
  ) {
    Participation participation = validateForParticipationOwner(id, participationId);
    participation.updateStatus(newStatus);
  }

  private Participation validateForParticipationOwner(Long id, Long participationId) {
    Participation participation = participationRepository.findById(participationId)
        .orElseThrow(() ->
            new ParticipationException(ParticipationErrorCode.PARTICIPATION_NOT_FOUND));

    if (!participation.isOwner(id)) {
      throw new ParticipationException(ParticipationErrorCode.NOT_PARTICIPATION_OWNER);
    }
    return participation;
  }
}
