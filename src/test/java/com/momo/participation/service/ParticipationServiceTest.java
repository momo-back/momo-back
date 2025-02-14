package com.momo.participation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.momo.chat.entity.ChatRoom;
import com.momo.chat.repository.ChatRoomRepository;
import com.momo.chat.service.ChatRoomService;
import com.momo.meeting.constant.FoodCategory;
import com.momo.meeting.constant.MeetingStatus;
import com.momo.meeting.entity.Meeting;
import com.momo.meeting.exception.MeetingErrorCode;
import com.momo.meeting.exception.MeetingException;
import com.momo.meeting.repository.MeetingRepository;
import com.momo.notification.constant.NotificationType;
import com.momo.notification.service.NotificationService;
import com.momo.participation.constant.ParticipationStatus;
import com.momo.participation.dto.AppliedMeetingDto;
import com.momo.participation.dto.AppliedMeetingsResponse;
import com.momo.participation.entity.Participation;
import com.momo.participation.exception.ParticipationErrorCode;
import com.momo.participation.exception.ParticipationException;
import com.momo.participation.projection.AppliedMeetingProjection;
import com.momo.participation.repository.ParticipationRepository;
import com.momo.user.entity.User;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ParticipationServiceTest {

  @Mock
  private MeetingRepository meetingRepository;

  @Mock
  private ParticipationRepository participationRepository;

  @Mock
  private ChatRoomRepository chatRoomRepository;

  @Mock
  private ChatRoomService chatRoomService;

  @Mock
  private NotificationService notificationService;

  @InjectMocks
  private ParticipationService participationService;

  @Test
  @DisplayName("모임 참여 신청 - 성공")
  void createParticipation_Success() {
    // given
    final String PARTICIPATION_NOTIFICATION_MESSAGE = "님이 모임 참여를 신청했습니다.";
    User user = createUser(1L);
    User meetingOwner = createUser(2L);
    Meeting meeting = createMeeting(meetingOwner, MeetingStatus.RECRUITING);
    Participation participation = createParticipation(user, meeting, ParticipationStatus.PENDING);

    // given
    when(meetingRepository.findById(meeting.getId())).thenReturn(Optional.of(meeting));
    when(participationRepository.existsByUser_IdAndMeeting_Id(user.getId(),
        meeting.getId())).thenReturn(false);
    when(participationRepository.save(any(Participation.class))).thenReturn(participation);

    // when
    participationService.createParticipation(user, meeting.getId());

    // then
    verify(participationRepository).save(any(Participation.class));
  }

  @Test
  @DisplayName("존재하지 않는 모임에 참가 신청 - 예외 발생")
  void validateForParticipate_ParticipateNotExistsMeeting_ThrowsException() {
    // given
    User user = createUser(1L);

    when(meetingRepository.findById(user.getId())).thenReturn(Optional.empty());

    // when
    assertThatThrownBy(() -> participationService.createParticipation(user, 1L)).isInstanceOf(
            MeetingException.class)
        .hasFieldOrPropertyWithValue("meetingErrorCode", MeetingErrorCode.MEETING_NOT_FOUND);

    // then
    verify(meetingRepository).findById(anyLong());
  }

  @Test
  @DisplayName("참여 불가능한 상태의 모임에 참가 신청 - 예외 발생")
  void validateForParticipate_InvalidMeetingStatus_ThrowsException() {
    // given
    User user = createUser(1L);
    Meeting meeting = createMeeting(user, MeetingStatus.CLOSED);

    when(meetingRepository.findById(anyLong())).thenReturn(Optional.of(meeting));

    // when
    // then
    assertThatThrownBy(
        () -> participationService.createParticipation(user, meeting.getId())).isInstanceOf(
        ParticipationException.class).hasFieldOrPropertyWithValue("participationErrorCode",
        ParticipationErrorCode.INVALID_MEETING_STATUS);
  }

  @Test
  @DisplayName("본인이 작성한 모임 참여 신청 - 예외 발생")
  void validateForParticipate_ParticipateSelfMeeting_ThrowsException() {
    // given
    User user = createUser(1L);
    Meeting meeting = createMeeting(user, MeetingStatus.RECRUITING);

    when(meetingRepository.findById(anyLong())).thenReturn(Optional.of(meeting));

    // when
    // then
    assertThatThrownBy(
        () -> participationService.createParticipation(user, meeting.getId())).isInstanceOf(
        ParticipationException.class).hasFieldOrPropertyWithValue("participationErrorCode",
        ParticipationErrorCode.PARTICIPATE_SELF_MEETING_NOT_ALLOW);
  }

  @Test
  @DisplayName("이미 참여 신청한 모임에 재신청시 예외가 발생한다")
  void validateForParticipate_WithAlreadyParticipated_ThrowsException() {
    // given
    User user = createUser(1L);
    User meetingOwner = createUser(2L);
    Meeting meeting = createMeeting(meetingOwner, MeetingStatus.RECRUITING);

    when(meetingRepository.findById(anyLong())).thenReturn(Optional.of(meeting));
    when(participationRepository.existsByUser_IdAndMeeting_Id(user.getId(),
        meeting.getId())).thenReturn(true);

    // when
    // then
    assertThatThrownBy(
        () -> participationService.createParticipation(user, meeting.getId()))
        .isInstanceOf(ParticipationException.class)
        .hasFieldOrPropertyWithValue("participationErrorCode",
            ParticipationErrorCode.ALREADY_PARTICIPATE_MEETING);

    verify(participationRepository).existsByUser_IdAndMeeting_Id(user.getId(), meeting.getId());
  }

  @Test
  @DisplayName("신청한 모임 목록 조회 - 성공")
  void getAppliedMeetings_Success() {
    // given
    Long userId = 1L;
    Long lastId = 0L;
    int pageSize = 10;

    List<AppliedMeetingProjection> projections = createMockProjections(pageSize);

    given(participationRepository.findAppliedMeetingsWithLastId(
        userId, lastId, pageSize + 1)).willReturn(projections);

    // when
    AppliedMeetingsResponse response =
        participationService.getAppliedMeetings(userId, lastId, pageSize);

    // then
    List<AppliedMeetingDto> appliedMeetings = response.getAppliedMeetings();
    assertThat(appliedMeetings).hasSize(pageSize);

    assertThatAppliedMeetings(appliedMeetings);
    verify(participationRepository)
        .findAppliedMeetingsWithLastId(userId, lastId, pageSize + 1);
  }

  @Test
  @DisplayName("참여 신청 승인  - 성공")
  void approveParticipation_Success() {
    // given
    User user = createUser(1L);
    User author = createUser(2L);
    Meeting meeting = createMeeting(author, MeetingStatus.RECRUITING);
    Participation participation = createParticipation(user, meeting, ParticipationStatus.PENDING);
    ChatRoom chatRoom = createChatRoom(author, meeting);

    when(participationRepository.findById(participation.getId()))
        .thenReturn(Optional.of(participation));
    when(chatRoomRepository.findByMeeting_Id(meeting.getId())).thenReturn(Optional.of(chatRoom));

    // when
    participationService.approveParticipation(author.getId(), participation.getId());

    // then
    assertEquals(MeetingStatus.RECRUITING, meeting.getMeetingStatus());
    assertEquals(ParticipationStatus.APPROVED, participation.getParticipationStatus());
    assertEquals(2, meeting.getApprovedCount());

    verify(participationRepository).findById(participation.getId());
    verify(chatRoomRepository).findByMeeting_Id(meeting.getId());
    verify(notificationService).sendNotification(
        participation.getUser(),
        participation.getMeeting().getTitle()
            + NotificationType.PARTICIPANT_APPROVED.getDescription(),
        NotificationType.PARTICIPANT_APPROVED);
  }

  @Test
  @DisplayName("참여 신청 거절  - 성공")
  void rejectParticipation_Success() {
    // given
    User user = createUser(1L);
    User author = createUser(2L);
    Meeting meeting = createMeeting(author, MeetingStatus.RECRUITING);
    Participation participation = createParticipation(user, meeting, ParticipationStatus.PENDING);

    when(participationRepository.findById(participation.getId()))
        .thenReturn(Optional.of(participation));

    // when
    participationService.rejectParticipation(author.getId(), participation.getId());

    // then
    assertEquals(MeetingStatus.RECRUITING, meeting.getMeetingStatus());
    assertEquals(ParticipationStatus.REJECTED, participation.getParticipationStatus());
    verify(participationRepository).findById(participation.getId());
  }

  private static ChatRoom createChatRoom(User host, Meeting meeting) {
    return ChatRoom.builder()
        .id(1L)
        .host(host)
        .meeting(meeting)
        .reader(new ArrayList<>(Collections.singletonList(host)))
        .build();
  }

  @Test
  @DisplayName("모임 참여 신청 삭제 - 성공")
  void deleteParticipation_Success() {
    // given
    User user = createUser(1L);
    Meeting meeting = createMeeting(user, MeetingStatus.RECRUITING);
    Participation participation = createParticipation(user, meeting, ParticipationStatus.PENDING);

    given(participationRepository.findById(participation.getId()))
        .willReturn(Optional.of(participation));

    // when
    participationService.deleteParticipation(user.getId(), participation.getId());

    // then
    verify(participationRepository, times(1)).delete(participation);
  }

  @Test
  @DisplayName("참여 신청의 소유자가 아닌 경우 - 예외 발생")
  void deleteParticipation_NotOwner_Throws() {
    // given
    User user = createUser(1L);
    User otherUser = createUser(2L);
    Meeting meeting = createMeeting(otherUser, MeetingStatus.RECRUITING);
    Participation participation = createParticipation(
        otherUser, meeting, ParticipationStatus.PENDING);

    given(participationRepository.findById(participation.getId()))
        .willReturn(Optional.of(participation));

    // when
    // then
    assertThatThrownBy(
        () -> participationService.deleteParticipation(user.getId(), participation.getId()))
        .isInstanceOf(ParticipationException.class)
        .hasFieldOrPropertyWithValue(
            "participationErrorCode", ParticipationErrorCode.NOT_PARTICIPATION_OWNER
        );

    verify(participationRepository, never()).delete(participation);
  }

  @Test
  @DisplayName("삭제할 수 없는 상태의 참여 신청 삭제 시도 - 예외 발생")
  void deleteParticipation_InvalidStatus_Throws() {
    //given
    User user = createUser(1L);
    Meeting meeting = createMeeting(user, MeetingStatus.RECRUITING);
    Participation participation = createParticipation(user, meeting, ParticipationStatus.APPROVED);

    given(participationRepository.findById(participation.getId()))
        .willReturn(Optional.of(participation));

    // when
    // then
    assertThatThrownBy(
        () -> participationService.deleteParticipation(user.getId(), participation.getId()))
        .isInstanceOf(ParticipationException.class)
        .hasFieldOrPropertyWithValue(
            "participationErrorCode", ParticipationErrorCode.INVALID_PARTICIPATION_STATUS
        );

    verify(participationRepository, never()).delete(participation);
  }

  private static User createUser(Long userId) {
    return User.builder().id(userId).email("test@gmail.com").password("testapssword")
        .phone("01012345678").enabled(true).verificationToken("asdasdsad").build();
  }

  private Meeting createMeeting(User user, MeetingStatus meetingStatus) {
    return Meeting.builder().id(user.getId()).user(user).title("테스트 모임").locationId(123456L)
        .latitude(32.123123).longitude(127.123123).address("테스트 주소")
        .meetingDateTime(LocalDateTime.now().plusDays(1)).maxCount(6).approvedCount(1)
        .category(Set.of(FoodCategory.KOREAN, FoodCategory.JAPANESE)).content("테스트 내용")
        .thumbnail("test-thumbnail-url.jpg").meetingStatus(meetingStatus).build();
  }

  private Participation createParticipation(User user, Meeting meeting, ParticipationStatus status) {
    return Participation.builder()
        .id(1L)
        .user(user)
        .meeting(meeting)
        .participationStatus(status)
        .build();
  }

  private List<AppliedMeetingProjection> createMockProjections(int pageSize) {
    List<AppliedMeetingProjection> projections = new ArrayList<>();
    for (int i = 0; i < pageSize + 1; i++) {
      createMockProjection(projections, i);
    }
    return projections;
  }

  private static void createMockProjection(List<AppliedMeetingProjection> projections, int i) {
    AppliedMeetingProjection projection = mock(AppliedMeetingProjection.class);
    given(projection.getId()).willReturn((long) i);
    given(projection.getMeetingId()).willReturn((long) i * 10);
    given(projection.getAuthorId()).willReturn((long) i * 100);
    given(projection.getParticipationStatus()).willReturn(ParticipationStatus.PENDING);
    given(projection.getTitle()).willReturn("Test Meeting " + i);
    given(projection.getLocationId()).willReturn((long) i);
    given(projection.getLatitude()).willReturn((double) i);
    given(projection.getLongitude()).willReturn((double) i);
    given(projection.getAddress()).willReturn("Test Address " + i);
    given(projection.getMeetingDateTime())
        .willReturn(LocalDateTime.now().minusHours(1 + i).truncatedTo(ChronoUnit.MINUTES));
    given(projection.getMaxCount()).willReturn(i + 2);
    given(projection.getApprovedCount()).willReturn(i + 1);
    given(projection.getCategory()).willReturn("KOREAN,DESSERT");
    given(projection.getContent()).willReturn("Test Content " + i);
    given(projection.getThumbnail()).willReturn("test_" + i + "_thumbnail_url.jpg");
    projections.add(projection);
  }

  private static void assertThatAppliedMeetings(List<AppliedMeetingDto> appliedMeetings) {
    for (int i = 0; i < appliedMeetings.size(); i++) {
      AssertThatAppliedMeeting(appliedMeetings, i);
    }
  }

  private static void AssertThatAppliedMeeting(List<AppliedMeetingDto> appliedMeetings, int i) {
    assertThat(appliedMeetings.get(i).getParticipationId()).isEqualTo(i);
    assertThat(appliedMeetings.get(i).getMeetingId()).isEqualTo(i * 10L);
    assertThat(appliedMeetings.get(i).getAuthorId()).isEqualTo(i * 100L);
    assertThat(appliedMeetings.get(i).getParticipationStatus())
        .isEqualTo(ParticipationStatus.PENDING);
    assertThat(appliedMeetings.get(i).getTitle()).isEqualTo("Test Meeting " + i);
    assertThat(appliedMeetings.get(i).getLocationId()).isEqualTo(i);
    assertThat(appliedMeetings.get(i).getLatitude()).isEqualTo(i);
    assertThat(appliedMeetings.get(i).getLongitude()).isEqualTo(i);
    assertThat(appliedMeetings.get(i).getAddress()).isEqualTo("Test Address " + i);
    assertThat(appliedMeetings.get(i).getMeetingDateTime())
        .isEqualTo(LocalDateTime.now().minusHours(1 + i).truncatedTo(ChronoUnit.MINUTES));
    assertThat(appliedMeetings.get(i).getMaxCount()).isEqualTo(i + 2);
    assertThat(appliedMeetings.get(i).getApprovedCount()).isEqualTo(i + 1);
    assertThat(appliedMeetings.get(i).getCategory()).isEqualTo(Set.of("한식", "디저트"));
    assertThat(appliedMeetings.get(i).getContent()).isEqualTo("Test Content " + i);
    assertThat(appliedMeetings.get(i).getThumbnail())
        .isEqualTo("test_" + i + "_thumbnail_url.jpg");
  }
}