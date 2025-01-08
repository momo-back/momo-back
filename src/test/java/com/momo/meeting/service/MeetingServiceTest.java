package com.momo.meeting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.momo.meeting.exception.MeetingErrorCode;
import com.momo.meeting.exception.MeetingException;
import com.momo.meeting.projection.MeetingToMeetingDtoProjection;
import com.momo.meeting.repository.MeetingRepository;
import com.momo.user.entity.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
class MeetingServiceTest {

  @Mock
  private MeetingRepository meetingRepository;

  @InjectMocks
  private MeetingService meetingService;

  @Test
  @DisplayName("모집글 작성 - 성공")
  void createMeeting_Success() {
    // given
    User user = createUser();
    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);
    MeetingCreateRequest request = createMeetingRequest();

    when(meetingRepository.countByUser_IdAndCreatedAtBetween(user.getId(), startOfDay, endOfDay))
        .thenReturn(0);

    // when
    MeetingCreateResponse response = meetingService.createMeeting(user, request);

    // then
    assertThat(response)
        .extracting(
            "title", "locationId",
            "latitude", "longitude",
            "address", "meetingDateTime",
            "maxCount", "approvedCount",
            "category", "content",
            "thumbnailUrl", "meetingStatus"
        ).containsExactly(
            request.getTitle(), request.getLocationId(),
            request.getLatitude(), request.getLongitude(),
            request.getAddress(), request.getMeetingDateTime(),
            request.getMaxCount(), 1,
            request.getCategory(), request.getContent(),
            request.getThumbnailUrl(), MeetingStatus.RECRUITING
        );

    verify(meetingRepository).countByUser_IdAndCreatedAtBetween(user.getId(), startOfDay, endOfDay);
  }

  private static final Double USER_LATITUDE = 37.502942;
  private static final Double USER_LONGITUDE = 126.947629;
  private static final int TEST_PAGE_SIZE = 10;

  @Test
  @DisplayName("모집글 목록 조회 기리 기준으로 정렬 - 성공")
  void getNearbyMeetings_Success() {
    // given
    MeetingsRequest request = createMeetingsRequest(
        USER_LATITUDE, USER_LONGITUDE, null, null, null);
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
    assertEquals(TEST_PAGE_SIZE, response.getMeetings().size());
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

  @Test
  @DisplayName("모집글 목록 조회 모임 날짜를 기준으로 정렬 - 성공")
  void getMeetingsByDate_Success() {
    // given
    MeetingsRequest request = createMeetingsRequest(
        null,
        null,
        null,
        null,
        LocalDateTime.now());
    List<MeetingToMeetingDtoProjection> mockProjections = createMockProjections();

    when(meetingRepository.findOrderByMeetingDateWithCursor(
        request.getCursorId(),
        request.getCursorMeetingDateTime(),
        request.getPageSize() + 1
    )).thenReturn(mockProjections);

    // when
    MeetingsResponse response = meetingService.getMeetings(request);

    // then
    assertEquals(TEST_PAGE_SIZE, response.getMeetings().size());
    verifyMeetingDtos(response.getMeetings());
    assertTrue(response.isHasNext());

    verifyCursor(response);

    verify(meetingRepository).findOrderByMeetingDateWithCursor(
        request.getCursorId(),
        request.getCursorMeetingDateTime(),
        request.getPageSize() + 1
    );
  }


  @Test
  @DisplayName("하루 게시글 제한(10개) 초과 - 예외 발생")
  void createMeeting_ExceedDailyLimit_ThrowsException() {
    // given
    User user = createUser();
    MeetingCreateRequest request = createMeetingRequest();
    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);

    when(meetingRepository.countByUser_IdAndCreatedAtBetween(user.getId(), startOfDay, endOfDay))
        .thenReturn(10);

    // when
    // then
    assertThatThrownBy(() -> meetingService.createMeeting(user, request))
        .isInstanceOf(MeetingException.class)
        .hasFieldOrPropertyWithValue(
            "meetingErrorCode",
            MeetingErrorCode.DAILY_POSTING_LIMIT_EXCEEDED);

    verify(meetingRepository).countByUser_IdAndCreatedAtBetween(eq(user.getId()), any(), any());
  }

  private static MeetingsRequest createMeetingsRequest(
      Double userLatitude,
      Double userLongitude,
      Long lastId,
      Double lastDistance,
      LocalDateTime lastMeetingDateTime
  ) {
    return MeetingsRequest.createRequest(
        userLatitude,
        userLongitude,
        null,
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
    when(projection.getAuthorId()).thenReturn((long) i * 100);
    when(projection.getTitle()).thenReturn("title" + i);
    when(projection.getLocationId()).thenReturn((long) i);
    when(projection.getLatitude()).thenReturn((double) i);
    when(projection.getLongitude()).thenReturn((double) i);
    when(projection.getAddress()).thenReturn("address" + i);
    when(projection.getMeetingDateTime())
        .thenReturn(LocalDateTime.now().plusDays(1 + i).truncatedTo(ChronoUnit.MINUTES));
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
    assertEquals(i * 100L, meetingDto.getAuthorId());
    assertEquals("title" + i, meetingDto.getTitle());
    assertEquals(i, meetingDto.getLocationId());
    assertEquals(i, meetingDto.getLatitude());
    assertEquals(i, meetingDto.getLongitude());
    assertEquals("address" + i, meetingDto.getAddress());
    assertEquals(LocalDateTime.now().plusDays(1 + i).truncatedTo(ChronoUnit.MINUTES),
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

  @Test
  @DisplayName("모임 수정 - 성공")
  void updateMeeting_Success() {
    // given
    User user = createUser();
    MeetingCreateRequest request = createMeetingRequest();
    Meeting meeting = createMeeting(user, request);
    MeetingCreateRequest updateRequest = createUpdateRequest();

    when(meetingRepository.findById(meeting.getId())).thenReturn(Optional.of(meeting));

    // when
    MeetingCreateResponse response =
        meetingService.updateMeeting(user.getId(), meeting.getId(), updateRequest);

    // then
    assertThat(response)
        .extracting(
            "id", "title",
            "locationId", "latitude",
            "longitude", "address",
            "meetingDateTime", "maxCount",
            "approvedCount", "category",
            "content", "thumbnailUrl",
            "meetingStatus"
        ).containsExactly(
            1L, updateRequest.getTitle(),
            updateRequest.getLocationId(), updateRequest.getLatitude(),
            updateRequest.getLongitude(), updateRequest.getAddress(),
            updateRequest.getMeetingDateTime(), updateRequest.getMaxCount(),
            1, updateRequest.getCategory(),
            updateRequest.getContent(), updateRequest.getThumbnailUrl(),
            MeetingStatus.RECRUITING
        );

    verify(meetingRepository).findById(meeting.getId());
  }

  @Test
  @DisplayName("존재하지 않는 모임 수정 - 예외 발생")
  void updateMeeting_MeetingNotFound_ThrowsException() {
    // given
    User user = createUser();
    MeetingCreateRequest request = createMeetingRequest();

    when(meetingRepository.findById(1L)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() ->
        meetingService.updateMeeting(user.getId(), 1L, request))
        .isInstanceOf(MeetingException.class)
        .hasFieldOrPropertyWithValue("meetingErrorCode", MeetingErrorCode.MEETING_NOT_FOUND);
  }

  @Test
  @DisplayName("모임 작성자가 아닌 경우 - 예외 발생")
  void updateMeeting_NotOwner_ThrowsException() {
    // given
    User user = createUser();
    MeetingCreateRequest request = createMeetingRequest();
    Meeting meeting = createMeeting(user, request);

    when(meetingRepository.findById(meeting.getId())).thenReturn(Optional.of(meeting));

    // when & then
    assertThatThrownBy(() ->
        meetingService.updateMeeting(2L, meeting.getId(), request))
        .isInstanceOf(MeetingException.class)
        .hasFieldOrPropertyWithValue("meetingErrorCode", MeetingErrorCode.NOT_MEETING_OWNER);
  }

  @Test
  @DisplayName("모임 상태 변경  - 성공")
  void updateMeetingStatus_Success() {
    // given
    User user = createUser();
    MeetingCreateRequest request = createMeetingRequest();
    Meeting meeting = createMeeting(user, request);

    when(meetingRepository.findById(user.getId())).thenReturn(Optional.of(meeting));

    // when
    meetingService.updateMeetingStatus(user.getId(), meeting.getId(), MeetingStatus.CLOSED);

    // then
    assertEquals(MeetingStatus.CLOSED, meeting.getMeetingStatus());
    verify(meetingRepository).findById(user.getId());
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
        .locationId(request.getLocationId())
        .latitude(request.getLatitude())
        .longitude(request.getLongitude())
        .address(request.getAddress())
        .meetingDateTime(request.getMeetingDateTime())
        .maxCount(request.getMaxCount())
        .approvedCount(1)
        .category(request.getCategory())
        .content(request.getContent())
        .thumbnailUrl(request.getThumbnailUrl())
        .meetingStatus(MeetingStatus.RECRUITING)
        .build();
  }

  private MeetingCreateRequest createMeetingRequest() {
    return MeetingCreateRequest.builder()
        .title("테스트 모임")
        .locationId(123456L)
        .latitude(37.123123)
        .longitude(127.123123)
        .address("테스트 주소")
        .meetingDateTime(LocalDateTime.now().plusDays(1))
        .maxCount(6)
        .category(Set.of(FoodCategory.KOREAN, FoodCategory.JAPANESE))
        .content("테스트 내용")
        .thumbnailUrl("test-thumbnail-url.jpg")
        .build();
  }

  private static MeetingCreateRequest createUpdateRequest() {
    return MeetingCreateRequest.builder()
        .title("업데이트 테스트 제목")
        .locationId(123456L)
        .latitude(37.123123)
        .longitude(127.123123)
        .address("테스트 주소")
        .meetingDateTime(LocalDateTime.now().plusHours(3))
        .maxCount(5)
        .category(Set.of(FoodCategory.DESSERT))
        .content("업데이트 내용")
        .thumbnailUrl("")
        .build();
  }
}