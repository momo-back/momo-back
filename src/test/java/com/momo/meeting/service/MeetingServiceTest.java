package com.momo.meeting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.momo.meeting.constant.FoodCategory;
import com.momo.meeting.constant.MeetingStatus;
import com.momo.meeting.dto.MeetingCursor;
import com.momo.meeting.dto.MeetingDto;
import com.momo.meeting.dto.MeetingsRequest;
import com.momo.meeting.dto.MeetingsResponse;
import com.momo.meeting.dto.create.MeetingCreateRequest;
import com.momo.meeting.dto.create.MeetingCreateResponse;
import com.momo.meeting.entity.Meeting;
import com.momo.meeting.projection.MeetingToMeetingDtoProjection;
import com.momo.meeting.repository.MeetingRepository;
import com.momo.meeting.validation.MeetingValidator;
import com.momo.user.entity.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
        //.categories(request.getCategories())
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

  private static final Double userLatitude = 37.502942;
  private static final Double userLongitude = 126.947629;
  private static final int TEST_PAGE_SIZE = 10;

  @Test
  @DisplayName("모집글 목록 조회 - 성공")
  void getNearbyMeetings_Success() {
    // given
    MeetingsRequest request = createaMeetingListReadRequest();
    List<MeetingToMeetingDtoProjection> mockProjections = createMockProjections();

    when(meetingRepository.findNearbyMeetingsWithCursor(
        request.getUserLatitude(),
        request.getUserLongitude(),
        request.getRadius(),
        request.getCursorId(),
        request.getCursorDistance(),
        request.getPageSize() + 1
    )).thenReturn(mockProjections);

    // when
    MeetingsResponse response = meetingService.getMeetings(request);

    // then
    assertEquals(TEST_PAGE_SIZE + 1, response.getMeetings().size());
    verifyMeetingDtos(response.getMeetings());
    assertTrue(response.isHasNext());

    verifyCursor(response);

    verify(meetingRepository).findNearbyMeetingsWithCursor(
        request.getUserLatitude(),
        request.getUserLongitude(),
        request.getRadius(),
        request.getCursorId(),
        request.getCursorDistance(),
        request.getPageSize() + 1
    );
  }

  private static MeetingsRequest createaMeetingListReadRequest() {
    return MeetingsRequest.createRequest(
        userLatitude,
        userLongitude,
        null,
        null,
        TEST_PAGE_SIZE
    );
  }

  private List<MeetingToMeetingDtoProjection> createMockProjections() {
    List<MeetingToMeetingDtoProjection> projections = new ArrayList<>();
    // 모의 데이터 생성 로직
    for (int i = 1; i <= TEST_PAGE_SIZE + 1; i++) { // pageSize + 1
      createMockProjection(projections, i);
    }
    return projections;
  }

  private static void createMockProjection(List<MeetingToMeetingDtoProjection> projections, int i) {
    MeetingToMeetingDtoProjection projection = mock(MeetingToMeetingDtoProjection.class);
    when(projection.getId()).thenReturn((long) i);
    when(projection.getTitle()).thenReturn("title" + i);
    when(projection.getLocationId()).thenReturn((long) i);
    when(projection.getLatitude()).thenReturn((double) i);
    when(projection.getLongitude()).thenReturn((double) i);
    when(projection.getAddress()).thenReturn("address" + i);
    when(projection.getMeetingDateTime())
        .thenReturn(LocalDateTime.of(2025, 1, 23, 3, 0)
            .plusDays(i));
    when(projection.getMaxCount()).thenReturn(2 + i);
    when(projection.getApprovedCount()).thenReturn(1 + i);
    when(projection.getCategory()).thenReturn("KOREAN,JAPANESE");
    when(projection.getThumbnailUrl()).thenReturn("test-url" + i + ".jpg");
    projections.add(projection);
  }

  private static void verifyMeetingDtos(List<MeetingDto> meetingDtos) {
    for (int i = 1; i < TEST_PAGE_SIZE + 1; i++) {
      verifyMeetingDto(meetingDtos, i);
    }
  }

  private static void verifyMeetingDto(List<MeetingDto> meetingDtos, int i) {
    MeetingDto meetingDto = meetingDtos.get(i - 1);
    assertEquals(i, meetingDto.getId());
    assertEquals("title" + i, meetingDto.getTitle());
    assertEquals(i, meetingDto.getLocationId());
    assertEquals(i, meetingDto.getLatitude());
    assertEquals(i, meetingDto.getLongitude());
    assertEquals("address" + i, meetingDto.getAddress());
    assertEquals(LocalDateTime.of(2025, 1, 23, 3, 0)
            .plusDays(i),
        meetingDto.getMeetingDateTime());
    assertEquals(2 + i, meetingDto.getMaxCount());
    assertEquals(1 + i, meetingDto.getApprovedCount());
    assertEquals(Set.of("한식", "일식"), meetingDto.getCategory());
    assertEquals("test-url" + i + ".jpg", meetingDto.getThumbnailUrl());
  }

  private static void verifyCursor(MeetingsResponse response) {
    List<MeetingDto> meetingDtos = response.getMeetings();
    MeetingDto meetingDto = meetingDtos.get(meetingDtos.size() - 1);
    MeetingCursor cursor = response.getCursor();
    assertEquals(cursor.getId(), meetingDto.getId());
  }
}