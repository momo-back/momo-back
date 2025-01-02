package com.momo.participation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.momo.meeting.constant.FoodCategory;
import com.momo.meeting.constant.MeetingStatus;
import com.momo.meeting.entity.Meeting;
import com.momo.participation.constant.ParticipationStatus;
import com.momo.participation.entity.Participation;
import com.momo.participation.repository.ParticipationRepository;
import com.momo.participation.validation.ParticipationValidation;
import com.momo.user.entity.User;
import java.time.LocalDateTime;
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
  private ParticipationValidation participationValidation;

  @Mock
  private ParticipationRepository participationRepository;

  @InjectMocks
  private ParticipationService participationService;

  @Test
  @DisplayName("모임 참여 신청 - 성공")
  void createParticipation_Success() {
    // given
    User user = createUser();
    Meeting meeting = createMeeting(user);
    Participation participation = createParticipation(user, meeting);

    // given
    when(participationValidation.validateForParticipate(user.getId(), meeting.getId()))
        .thenReturn(meeting);

    when(participationRepository.save(any(Participation.class))).thenReturn(participation);

    // when
    Long participationId = participationService.createParticipation(user, meeting.getId());

    // then
    assertThat(participationId).isEqualTo(participation.getId());

    verify(participationValidation).validateForParticipate(user.getId(), meeting.getId());
    verify(participationRepository).save(any(Participation.class));
  }

  private static User createUser() {
    return User.builder()
        .id(1L)
        .email("test@gmail.com")
        .password("testapssword")
        .phone("01012345678")
        .enabled(true)
        .verificationToken("asdasdsad")
        .build();
  }

  private Meeting createMeeting(User user) {
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
        .meetingStatus(MeetingStatus.RECRUITING)
        .build();
  }

  private Participation createParticipation(User user, Meeting meeting) {
    return Participation.builder()
        .id(1L)
        .user(user)
        .meeting(meeting)
        .participationStatus(ParticipationStatus.PENDING)
        .build();
  }
}