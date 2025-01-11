package com.momo.participation.service;

import com.momo.chat.entity.ChatRoom;
import com.momo.chat.exception.ChatErrorCode;
import com.momo.chat.exception.ChatException;
import com.momo.chat.repository.ChatRoomRepository;
import com.momo.chat.service.ChatRoomService;
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
import com.momo.participation.projection.AppliedMeetingProjection;
import com.momo.participation.repository.ParticipationRepository;
import com.momo.user.entity.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParticipationService {

  private final ParticipationRepository participationRepository;
  private final MeetingRepository meetingRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final ChatRoomService chatRoomService;
  private final NotificationService notificationService;

  public void createParticipation(User user, Long meetingId) {
    Meeting meeting = validateForParticipate(user.getId(), meetingId);

    Participation participation = Participation.createMeetingParticipant(user, meeting);
    participationRepository.save(participation);

    // 모임 주최자에게 새로운 참여 알림 발송
    sendNotificationToAuthor(user);
  }

  public AppliedMeetingsResponse getAppliedMeetings(Long userId, Long lastId, int pageSize) {
    List<AppliedMeetingProjection> appliedMeetingsProjections =
        participationRepository.findAppliedMeetingsWithLastId(userId, lastId, pageSize + 1);
    // 다음 페이지 존재 여부를 알기 위해 + 1

    return AppliedMeetingsResponse.of(
        appliedMeetingsProjections,
        pageSize
    );
  }

  public void cancelParticipation(Long userId, Long participationId) {
    Participation participation = validateForCancelParticipation(userId, participationId);
    participationRepository.delete(participation);
  }

  public void approveParticipation(Long authorId, Long participationId) {
    Participation participation = updateAppoveParticipation(authorId, participationId);
    joinChatRoom(participation.getMeeting(), participation); // 채팅방 입장

    // 참여 신청을 보낸 회원에게 알림 발송 TODO: 비동기 처리 고려
    sendNotificationToAppliedUser(participation);
  }

  public void rejectParticipation(Long authorId, Long participationId) {
    Participation participation = updateRejectParticipation(authorId, participationId);
    sendNotificationToAppliedUser(participation); // 참여 신청을 보낸 회원에게 알림 발송
  }

  @Transactional
  Participation updateAppoveParticipation(Long authorId, Long participationId) {
    Participation participation = validateMeetingAuthorForParticipation(authorId, participationId);
    Meeting meeting = participation.getMeeting();

    validatePossibleParticipant(participation, meeting); // 검증
    participation.updateStatus(ParticipationStatus.APPROVED); // 참여 신청 상태를 APPROVED로 변경
    meeting.incrementApprovedCount(); // 현재 인원 증가

    return participation;
  }

  @Transactional
  Participation updateRejectParticipation(Long authorId, Long participationId) {
    Participation participation = validateMeetingAuthorForParticipation(authorId, participationId);
    participation.updateStatus(ParticipationStatus.REJECTED);
    return participation;
  }

  private void sendNotificationToAuthor(User user) {
    notificationService.sendNotification(
        user,
        user.getNickname() + NotificationType.NEW_PARTICIPATION_REQUEST.getDescription(),
        NotificationType.NEW_PARTICIPATION_REQUEST
    );
  }

  private Meeting validateForParticipate(Long userId, Long meetingId) {
    Meeting meeting = findByMeetingId(meetingId);

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

  private Participation validateForCancelParticipation(Long userId, Long participationId) {
    // 참여 신청이 존재하는지 검증
    Participation participation = participationRepository.findById(participationId)
        .orElseThrow(() ->
            new ParticipationException(ParticipationErrorCode.PARTICIPATION_NOT_FOUND));

    // 참여 신청의 주인인지 검증
    if (!participation.isAuthor(userId)) {
      throw new ParticipationException(ParticipationErrorCode.NOT_PARTICIPATION_OWNER);
    }

    // 참여 신청을 취소할 수 있는 상태인지 검증
    if (!(participation.getParticipationStatus() == ParticipationStatus.PENDING)) {
      throw new ParticipationException(ParticipationErrorCode.INVALID_PARTICIPATION_STATUS);
    }

    Meeting meeting = findByMeetingId(participation.getMeetingId()); // 모임이 존재하는지 검증

    // 모임 상태가 모임 신청을 취소할 수 있는 상태인지 검증
    if (!(meeting.getMeetingStatus() == MeetingStatus.RECRUITING)) {
      throw new MeetingException(MeetingErrorCode.INVALID_MEETING_STATUS);
    }
    return participation;
  }

  private Meeting findByMeetingId(Long meetingId) {
    return meetingRepository.findById(meetingId)
        .orElseThrow(() -> new MeetingException(MeetingErrorCode.MEETING_NOT_FOUND));
  }

  private void sendNotificationToAppliedUser(Participation participation) {
    notificationService.sendNotification(
        participation.getUser(),
        participation.getMeeting().getTitle()
            + NotificationType.PARTICIPANT_APPROVED.getDescription(),
        NotificationType.PARTICIPANT_APPROVED
    );
  }

  private void joinChatRoom(Meeting meeting, Participation participation) {
    ChatRoom chatRoom = chatRoomRepository.findByMeeting_Id(meeting.getId())
        .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

    chatRoomService.joinRoom(participation.getUserId(), chatRoom.getId());
  }

  private static void validatePossibleParticipant(Participation participation, Meeting meeting) {
    // 참여 상태가 모임에 참여가 가능한 상태인지 확인
    if (!(participation.isAcceptableStatus())) {
      throw new ParticipationException(ParticipationErrorCode.INVALID_PARTICIPATION_STATUS);
    }

    // 모임이 참여 가능 상태인지 검증
    if (!meeting.isRecruiting()) {
      throw new MeetingException(MeetingErrorCode.INVALID_MEETING_STATUS);
    }
  }

  private Participation validateMeetingAuthorForParticipation(Long authorId, Long participationId) {
    // 존재하는 참여 신청인지 확인
    Participation participation = participationRepository.findById(participationId)
        .orElseThrow(() ->
            new ParticipationException(ParticipationErrorCode.PARTICIPATION_NOT_FOUND));

    // 현재 회원이 해당 모임 신청을 받은 모임의 작성자인지 검증
    if (!participation.isMeetingAuthor(authorId)) {
      throw new MeetingException(MeetingErrorCode.NOT_MEETING_OWNER);
    }
    return participation;
  }
}
