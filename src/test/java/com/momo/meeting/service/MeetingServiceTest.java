package com.momo.meeting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.momo.exception.MeetingErrorCode;
import com.momo.exception.MeetingException;
import com.momo.meeting.constant.FoodCategory;
import com.momo.meeting.constant.MeetingStatus;
import com.momo.meeting.dto.MeetingCreateRequest;
import com.momo.meeting.dto.MeetingCreateResponse;
import com.momo.meeting.persist.entity.Meeting;
import com.momo.meeting.persist.repository.MeetingRepository;
import com.momo.meeting.validator.MeetingValidator;
import com.momo.user.entity.User;
import com.momo.user.repository.UserRepository;
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

  @Mock
  private MeetingRepository meetingRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private MeetingValidator meetingValidator;

  @InjectMocks
  private MeetingService meetingService;

  @Test
  @DisplayName("모집글 작성 - 성공")
  void createMeeting_Success() {
    // given
    User user = createUser();
    MeetingCreateRequest request = createValidRequest();
    Meeting meeting = createMeeting(user, request);

    when(meetingRepository.save(any(Meeting.class))).thenReturn(meeting);

    // when
    MeetingCreateResponse response = meetingService.createMeeting(user, request);

    // then
    verify(meetingRepository).save(any(Meeting.class));
    assertThat(response)
        .extracting(
            "id", "user", "title",
            "meetingDateTime", "approvedCount", "maxCount",
            "locationId", "categories", "content",
            "thumbnailUrl", "meetingStatus"
        ).containsExactly(1L, user, response.getTitle(),
            response.getMeetingDateTime(), response.getApprovedCount(), response.getMaxCount(),
            response.getLocationId(), response.getCategories(), response.getContent(),
            response.getThumbnailUrl(), response.getMeetingStatus());
  }

  @Test
  @DisplayName("하루 게시글 제한(10개) 초과 - 예외 발생")
  void createMeetingPost_ExceedDailyLimit_ThrowsException() {
    // given
    User user = createUser();
    MeetingCreateRequest request = createValidRequest();

    when(meetingRepository.countByUser_IdAndCreatedAtBetween(eq(user.getId()), any(), any()))
        .thenReturn(10);

    // when & then
    assertThatThrownBy(() ->
        meetingService.createMeeting(user, request))
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
    User mockUser = mock(User.class);
    MeetingCreateRequest request = createValidRequest();

    // when & then
    assertThatThrownBy(() ->
        meetingService.createMeeting(mockUser, request))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("미래가 아닌 모임 날짜 - 예외 발생")
  void createMeetingPost_PastDateTime_ThrowsException() {
    // given
    User mockUser = mock(User.class);
    MeetingCreateRequest request = createRequestWithInvalidDateTime();

    // when & then
    assertThatThrownBy(() ->
        meetingService.createMeeting(mockUser, request))
        .isInstanceOf(MeetingException.class)
        .hasFieldOrPropertyWithValue(
            "meetingErrorCode", MeetingErrorCode.INVALID_MEETING_DATE_TIME);
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

  private static Meeting createMeeting(User user, MeetingCreateRequest request) {
    return Meeting.builder()
        .id(1L)
        .user(user)
        .title(request.getTitle())
        .meetingDateTime(request.getMeetingDateTime())
        .approvedCount(1)
        .maxCount(request.getMaxCount())
        .locationId(request.getLocationId())
        .categories(request.getCategories())
        .content(request.getContent())
        .thumbnailUrl(request.getThumbnailUrl())
        .meetingStatus(MeetingStatus.RECRUITING)
        .build();
  }

  private MeetingCreateRequest createValidRequest() {
    return MeetingCreateRequest.builder()
        .title("테스트 모임")
        .meetingDateTime(LocalDateTime.now().plusDays(1))
        .locationId(123456L)
        .maxCount(6)
        .categories(Set.of(FoodCategory.KOREAN, FoodCategory.JAPANESE))
        .content("테스트 내용")
        .thumbnailUrl("test-thumbnail-url.jpg")
        .build();
  }

  private MeetingCreateRequest createRequestWithInvalidDateTime() {
    return MeetingCreateRequest.builder()
        .title("테스트 모임")
        .meetingDateTime(LocalDateTime.now().minusDays(1))
        .locationId(123456L)
        .maxCount(6)
        .categories(Set.of(FoodCategory.KOREAN, FoodCategory.JAPANESE))
        .content("테스트 내용")
        .thumbnailUrl("test-thumbnail-url.jpg")
        .build();
  }
}