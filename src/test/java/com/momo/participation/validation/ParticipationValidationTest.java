package com.momo.participation.validation;


import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.momo.meeting.constant.FoodCategory;
import com.momo.meeting.constant.MeetingStatus;
import com.momo.meeting.entity.Meeting;
import com.momo.meeting.exception.MeetingErrorCode;
import com.momo.meeting.exception.MeetingException;
import com.momo.meeting.repository.MeetingRepository;
import com.momo.participation.exception.ParticipationErrorCode;
import com.momo.participation.exception.ParticipationException;
import com.momo.participation.repository.ParticipationRepository;
import com.momo.user.entity.User;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ParticipationValidationTest {

  @Mock
  private MeetingRepository meetingRepository;

  @Mock
  private ParticipationRepository participationRepository;

  @InjectMocks
  private ParticipationValidator participationValidation;

  @Test
  @DisplayName("존재하지 않는 모임에 참가 신청 - 예외 발생")
  void validateForParticipate_ParticipateNotExistsMeeting_ThrowsException() {
    // given
    User user = createUser(1L);

    when(meetingRepository.findById(anyLong()))
        .thenReturn(Optional.empty());

    // when
    // then
    assertThatThrownBy(() -> participationValidation.validateForParticipate(
        user.getId(), 1L))
        .isInstanceOf(MeetingException.class)
        .hasFieldOrPropertyWithValue(
            "meetingErrorCode", MeetingErrorCode.MEETING_NOT_FOUND);

    verify(meetingRepository).findById(anyLong());
  }

  @Test
  @DisplayName("참여 불가능한 상태의 모임글에 참가 신청 - 예외 발생")
  void validateForParticipate_InvalidMeetingStatus_ThrowsException() {
    // given
    User user = createUser(1L);
    Meeting meeting = createMeeting(user, MeetingStatus.CLOSED);

    when(meetingRepository.findById(anyLong()))
        .thenReturn(Optional.of(meeting));

    // when
    // then
    assertThatThrownBy(() -> participationValidation.validateForParticipate(
        user.getId(), meeting.getId()))
        .isInstanceOf(ParticipationException.class)
        .hasFieldOrPropertyWithValue(
            "participationErrorCode", ParticipationErrorCode.INVALID_MEETING_STATUS);
  }

  @Test
  @DisplayName("본인이 작성한 모임글에 참여 신청 - 예외 발생")
  void validateForParticipate_ParticipateSelfMeeting_ThrowsException() {
    // given
    User user = createUser(1L);
    Meeting meeting = createMeeting(user, MeetingStatus.RECRUITING);

    when(meetingRepository.findById(anyLong()))
        .thenReturn(Optional.of(meeting));

    // when
    // then
    assertThatThrownBy(() -> participationValidation.validateForParticipate(
        user.getId(), meeting.getId()))
        .isInstanceOf(ParticipationException.class)
        .hasFieldOrPropertyWithValue(
            "participationErrorCode",
            ParticipationErrorCode.PARTICIPATE_SELF_MEETING_NOT_ALLOW
        );
  }

  @Test
  @DisplayName("이미 참여 신청한 모임에 재신청시 예외가 발생한다")
  void validateForParticipate_WithAlreadyParticipated_ThrowsException() {
    // given
    User participationUser = createUser(1L);
    User meetingOwnerUser = createUser(2L);
    Meeting meeting = createMeeting(meetingOwnerUser, MeetingStatus.RECRUITING);

    when(meetingRepository.findById(anyLong()))
        .thenReturn(Optional.of(meeting));
    when(participationRepository.existsByUser_IdAndMeeting_Id(
        participationUser.getId(), meeting.getId())).thenReturn(true);

    // when
    // then
    assertThatThrownBy(() -> participationValidation.validateForParticipate(
        participationUser.getId(), meeting.getId()))
        .isInstanceOf(ParticipationException.class)
        .hasFieldOrPropertyWithValue(
            "participationErrorCode",
            ParticipationErrorCode.ALREADY_PARTICIPATE_MEETING
        );

    verify(participationRepository)
        .existsByUser_IdAndMeeting_Id(participationUser.getId(), meeting.getId());
  }

  private static User createUser(Long userId) {
    return User.builder()
        .id(userId)
        .email("test@gmail.com")
        .password("testapssword")
        .phone("01012345678")
        .enabled(true)
        .verificationToken("asdasdsad")
        .build();
  }

  private Meeting createMeeting(User user, MeetingStatus status) {
    return Meeting.builder()
        .id(user.getId())
        .user(user)
        .title("테스트 모임")
        .locationId(123456L)
        .latitude(32.123123)
        .longitude(127.123123)
        .address("테스트 주소")
        .meetingDateTime(LocalDateTime.now().plusDays(1))
        .maxCount(6)
        .approvedCount(1)
        .category(Set.of(FoodCategory.KOREAN, FoodCategory.JAPANESE))
        .content("테스트 내용")
        .thumbnailUrl("test-thumbnail-url.jpg")
        .meetingStatus(status)
        .build();
  }
}