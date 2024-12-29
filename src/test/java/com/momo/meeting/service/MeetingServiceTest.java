package com.momo.meeting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.momo.meeting.exception.MeetingErrorCode;
import com.momo.meeting.exception.MeetingException;
import com.momo.meeting.constant.FoodCategory;
import com.momo.meeting.dto.CreateMeetingRequest;
import com.momo.meeting.entity.Meeting;
import com.momo.meeting.repository.MeetingRepository;
import com.momo.mock.MockUser;
import com.momo.mock.MockUserRepository;
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
class MeetingServiceTest {

  @InjectMocks
  private MeetingService meetingService;

  @Mock
  private MeetingRepository meetingRepository;

  @Mock
  private MockUserRepository userRepository;

  @Test
  @DisplayName("모집글 작성 - 성공")
  void createMeeting_Success() {
    // given
    CreateMeetingRequest request = createValidRequest();
    MockUser mockUser = mock(MockUser.class);
    Meeting mockMeeting = mock(Meeting.class);

    when(meetingRepository.countByUser_IdAndCreatedAtBetween(eq(1L), any(), any()))
        .thenReturn(0);
    when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
    when(meetingRepository.save(any(Meeting.class))).thenReturn(mockMeeting);
    when(mockMeeting.getId()).thenReturn(1L);

    // when
    Long meetingId = meetingService.createMeeting(request, 1L);

    // then
    verify(meetingRepository).countByUser_IdAndCreatedAtBetween(eq(1L), any(), any());
    verify(userRepository).findById(1L);
    verify(meetingRepository).save(any(Meeting.class));
    assertThat(meetingId).isNotNull();
  }

  @Test
  @DisplayName("하루 게시글 제한(10개) 초과 - 예외 발생")
  void createMeetingPost_ExceedDailyLimit_ThrowsException() {
    // given
    CreateMeetingRequest request = createValidRequest();
    when(meetingRepository
        .countByUser_IdAndCreatedAtBetween(anyLong(), any(), any())
    ).thenReturn(10);

    // when & then
    assertThatThrownBy(() ->
        meetingService.createMeeting(request, 1L))
        .isInstanceOf(MeetingException.class)
        .hasFieldOrPropertyWithValue(
            "meetingErrorCode",
            MeetingErrorCode.DAILY_POSTING_LIMIT_EXCEEDED
        );
  }

  @Test
  @DisplayName("존재하지 않는 사용자 - 예외 발생")
  void createMeetingPost_UserNotFound_ThrowsException() {
    // given
    CreateMeetingRequest request = createValidRequest();
    when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() ->
        meetingService.createMeeting(request, 1L))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("미래가 아닌 모임 날짜 - 예외 발생")
  void createMeetingPost_PastDateTime_ThrowsException() {
    // given
    CreateMeetingRequest request = createRequestWithInvalidDateTime();

    // when & then
    assertThatThrownBy(() ->
        meetingService.createMeeting(request, 1L))
        .isInstanceOf(MeetingException.class)
        .hasFieldOrPropertyWithValue(
            "meetingErrorCode", MeetingErrorCode.INVALID_MEETING_DATE_TIME);
  }

  private CreateMeetingRequest createValidRequest() {
    return CreateMeetingRequest.builder()
        .title("테스트 모임")
        .meetingDateTime(LocalDateTime.now().plusDays(1))
        .locationId(123456L)
        .maxParticipants(6)
        .categories(Set.of(FoodCategory.KOREAN))
        .content("테스트 내용")
        .build();
  }

  private CreateMeetingRequest createRequestWithInvalidDateTime() {
    return CreateMeetingRequest.builder()
        .title("테스트 모임")
        .meetingDateTime(LocalDateTime.now().minusDays(1))
        .locationId(123456L)
        .maxParticipants(6)
        .categories(Set.of(FoodCategory.KOREAN))
        .content("테스트 내용")
        .build();
  }
}