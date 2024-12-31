package com.momo.meeting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.momo.meeting.constant.FoodCategory;
import com.momo.meeting.constant.MeetingStatus;
import com.momo.meeting.dto.MeetingCreateRequest;
import com.momo.meeting.dto.MeetingCreateResponse;
import com.momo.meeting.entity.Meeting;
import com.momo.meeting.exception.MeetingErrorCode;
import com.momo.meeting.exception.MeetingException;
import com.momo.meeting.repository.MeetingRepository;
import com.momo.meeting.validator.MeetingValidator;
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
class MeetingServiceTest {

  @Mock
  private MeetingRepository meetingRepository;

  @Mock
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

    doNothing().when(meetingValidator).validateForMeetingCreation(eq(user.getId()), any());
    when(meetingRepository.save(any(Meeting.class))).thenReturn(meeting);

    // when
    MeetingCreateResponse response = meetingService.createMeeting(user, request);

    // then
    verify(meetingRepository).save(any(Meeting.class));
    assertThat(response)
        .extracting(
            "id", "title",
            "meetingDateTime", "approvedCount", "maxCount",
            "locationId", "categories", "content",
            "thumbnailUrl", "meetingStatus"
        ).containsExactly(
            1L, response.getTitle(),
            response.getMeetingDateTime(), response.getApprovedCount(),
            response.getMaxCount(), response.getLocationId(),
            response.getCategories(), response.getContent(),
            response.getThumbnailUrl(), response.getMeetingStatus());
  }

  @Test
  @DisplayName("존재하지 않는 사용자 - 예외 발생")
  void createMeeting_UserNotFound_ThrowsException() {
    // given
    User mockUser = mock(User.class);
    MeetingCreateRequest request = createValidRequest();

    // when & then
    assertThatThrownBy(() ->
        meetingService.createMeeting(mockUser, request))
        .isInstanceOf(RuntimeException.class);
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
}