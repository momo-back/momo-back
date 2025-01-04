package com.momo.meeting.validator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.momo.meeting.constant.FoodCategory;
import com.momo.meeting.dto.create.MeetingCreateRequest;
import com.momo.meeting.exception.MeetingErrorCode;
import com.momo.meeting.exception.MeetingException;
import com.momo.meeting.repository.MeetingRepository;
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
class MeetingValidatorTest {

  @Mock
  private MeetingRepository meetingRepository;

  @InjectMocks
  private MeetingValidator meetingValidator;

  @Test
  @DisplayName("하루 게시글 제한(10개) 초과 - 예외 발생")
  void createMeeting_ExceedDailyLimit_ThrowsException() {
    // given
    User user = createUser();
    MeetingCreateRequest request = createValidRequest();

    when(meetingRepository.countByUser_IdAndCreatedAtBetween(eq(user.getId()), any(), any()))
        .thenReturn(10);

    // when
    // then
    assertThatThrownBy(() -> meetingValidator.validateForMeetingCreation(
        user.getId(), LocalDateTime.now()))
        .isInstanceOf(MeetingException.class)
        .hasFieldOrPropertyWithValue(
            "meetingErrorCode",
            MeetingErrorCode.DAILY_POSTING_LIMIT_EXCEEDED);

    verify(meetingRepository).countByUser_IdAndCreatedAtBetween(eq(user.getId()), any(), any());
  }

  @Test
  @DisplayName("미래가 아닌 모임 날짜 - 예외 발생")
  void createMeeting_PastDateTime_ThrowsException() {
    // given
    User user = createUser();
    MeetingCreateRequest request = createRequestWithInvalidDateTime();

    // when & then
    assertThatThrownBy(() -> meetingValidator.validateForMeetingCreation(
        user.getId(), request.getMeetingDateTime()))
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