package com.momo.meeting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.momo.chat.entity.ChatRoom;
import com.momo.chat.repository.ChatRoomRepository;
import com.momo.chat.service.ChatRoomService;
import com.momo.image.service.ImageService;
import com.momo.meeting.constant.FoodCategory;
import com.momo.meeting.constant.MeetingStatus;
import com.momo.meeting.dto.MeetingUpdateRequest;
import com.momo.meeting.dto.MeetingStatusRequest;
import com.momo.meeting.dto.createdMeeting.CreatedMeetingDto;
import com.momo.meeting.dto.createdMeeting.CreatedMeetingsResponse;
import com.momo.meeting.dto.MeetingCursor;
import com.momo.meeting.dto.MeetingDto;
import com.momo.meeting.dto.MeetingsRequest;
import com.momo.meeting.dto.MeetingsResponse;
import com.momo.meeting.dto.MeetingCreateRequest;
import com.momo.meeting.dto.MeetingResponse;
import com.momo.meeting.entity.Meeting;
import com.momo.meeting.exception.MeetingErrorCode;
import com.momo.meeting.exception.MeetingException;
import com.momo.meeting.projection.CreatedMeetingProjection;
import com.momo.meeting.projection.MeetingParticipantProjection;
import com.momo.meeting.projection.MeetingToMeetingDtoProjection;
import com.momo.meeting.repository.MeetingRepository;
import com.momo.notification.service.NotificationService;
import com.momo.participation.constant.ParticipationStatus;
import com.momo.participation.repository.ParticipationRepository;
import com.momo.user.entity.User;
import java.time.LocalDate;
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
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {

  @Mock
  private MeetingRepository meetingRepository;

  @Mock
  private ParticipationRepository participationRepository;

  @Mock
  private ChatRoomRepository chatRoomRepository;

  @Mock
  private ChatRoomService chatRoomService;

  @Mock
  private TransactionTemplate transactionTemplate;

  @Mock
  private NotificationService notificationService;

  @Mock
  private ImageService imageService;

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
    MultipartFile image = mock(MultipartFile.class);
    String imageUrl = "test-image.jpg";

    when(imageService.uploadImageProcess(image)).thenReturn(imageUrl);
    when(meetingRepository.countByUser_IdAndCreatedAtBetween(user.getId(), startOfDay, endOfDay))
        .thenReturn(0);

    // when
    MeetingResponse response = meetingService.createMeeting(user, request, image);

    // then
    assertThat(response)
        .extracting(
            "title", "locationId",
            "latitude", "longitude",
            "address", "meetingDateTime",
            "maxCount", "approvedCount",
            "category", "content",
            "thumbnail", "meetingStatus"
        ).containsExactly(
            request.getTitle(), request.getLocationId(),
            request.getLatitude(), request.getLongitude(),
            request.getAddress(), request.getMeetingDateTime(),
            request.getMaxCount(), 1,
            request.getCategory(), request.getContent(),
            imageUrl, MeetingStatus.RECRUITING
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
  @DisplayName("모임 날짜 1년 이후로 설정 - 예외 발생")
  void createMeeting_AfterOneYear_ThrowsException() {
    // given
    User user = createUser();
    MeetingCreateRequest request = createMeetingInvalidMeetingDateRequest();
    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);
    MultipartFile image = mock(MultipartFile.class);

    when(meetingRepository.countByUser_IdAndCreatedAtBetween(user.getId(), startOfDay, endOfDay))
        .thenReturn(0);

    // when
    // then
    assertThatThrownBy(() -> meetingService.createMeeting(user, request, image))
        .isInstanceOf(MeetingException.class)
        .hasFieldOrPropertyWithValue(
            "meetingErrorCode",
            MeetingErrorCode.INVALID_MEETING_DATE);

    verify(meetingRepository).countByUser_IdAndCreatedAtBetween(user.getId(), startOfDay, endOfDay);
  }

  @Test
  @DisplayName("하루 게시글 제한(10개) 초과 - 예외 발생")
  void createMeeting_ExceedDailyLimit_ThrowsException() {
    // given
    User user = createUser();
    MeetingCreateRequest request = createMeetingRequest();
    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);
    MultipartFile image = mock(MultipartFile.class);

    when(meetingRepository.countByUser_IdAndCreatedAtBetween(user.getId(), startOfDay, endOfDay))
        .thenReturn(10);

    // when
    // then
    assertThatThrownBy(() -> meetingService.createMeeting(user, request, image))
        .isInstanceOf(MeetingException.class)
        .hasFieldOrPropertyWithValue(
            "meetingErrorCode",
            MeetingErrorCode.DAILY_POSTING_LIMIT_EXCEEDED);

    verify(meetingRepository).countByUser_IdAndCreatedAtBetween(eq(user.getId()), any(), any());
  }

  @Test
  @DisplayName("모임 수정 - 성공")
  void updateMeeting_Success() {
    // given
    User user = createUser();
    MeetingCreateRequest request = createMeetingRequest();
    Meeting meeting = createMeeting(user, request);
    MultipartFile updateThumbnail = mock(MultipartFile.class);
    String updateThumbnailUrl = "test-thumbnail.jpg";
    MeetingUpdateRequest updateRequest = createUpdateRequest(updateThumbnailUrl);

    when(imageService.handleThumbnailUpdate(meeting.getThumbnail(), updateThumbnail))
        .thenReturn(updateThumbnailUrl);
    when(meetingRepository.findById(meeting.getId())).thenReturn(Optional.of(meeting));

    // when
    MeetingResponse response =
        meetingService.updateMeeting(user.getId(), meeting.getId(), updateRequest, updateThumbnail);

    // then
    assertThat(response)
        .extracting(
            "id", "title",
            "locationId", "latitude",
            "longitude", "address",
            "meetingDateTime", "maxCount",
            "approvedCount", "category",
            "content", "thumbnail",
            "meetingStatus"
        ).containsExactly(
            1L, updateRequest.getTitle(),
            updateRequest.getLocationId(), updateRequest.getLatitude(),
            updateRequest.getLongitude(), updateRequest.getAddress(),
            updateRequest.getMeetingDateTime(), updateRequest.getMaxCount(),
            1, updateRequest.getCategory(),
            updateRequest.getContent(), updateRequest.getThumbnail(),
            MeetingStatus.RECRUITING
        );

    verify(meetingRepository).findById(meeting.getId());
  }

  @Test
  @DisplayName("존재하지 않는 모임 수정 - 예외 발생")
  void updateMeeting_MeetingNotFound_ThrowsException() {
    // given
    User user = createUser();
    MeetingUpdateRequest request = createUpdateRequest("test_thumbnail.jpg");

    when(meetingRepository.findById(1L)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() ->
        meetingService.updateMeeting(user.getId(), 1L, request, null))
        .isInstanceOf(MeetingException.class)
        .hasFieldOrPropertyWithValue("meetingErrorCode", MeetingErrorCode.MEETING_NOT_FOUND);
  }

  @Test
  @DisplayName("모임 작성자가 아닌 경우 - 예외 발생")
  void updateMeeting_NotOwner_ThrowsException() {
    // given
    User user = createUser();
    MultipartFile updateThumbnail = mock(MultipartFile.class);
    String updateThumbnailUrl = "test-thumbnail.jpg";
    MeetingUpdateRequest request = createUpdateRequest(updateThumbnailUrl);
    Meeting meeting = createMeeting(user, request);

    when(meetingRepository.findById(meeting.getId())).thenReturn(Optional.of(meeting));

    // when & then
    assertThatThrownBy(() ->
        meetingService.updateMeeting(2L, meeting.getId(), request, updateThumbnail))
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
    MeetingStatusRequest meetingStatus = MeetingStatusRequest.builder()
        .meetingStatus(MeetingStatus.CLOSED)
        .build();

    when(meetingRepository.findById(user.getId())).thenReturn(Optional.of(meeting));

    // when
    meetingService.updateMeetingStatus(user.getId(), meeting.getId(), meetingStatus);

    // then
    assertEquals(MeetingStatus.CLOSED, meeting.getMeetingStatus());
    verify(meetingRepository).findById(user.getId());
  }

  @Test
  @DisplayName("작성한 모임 목록 조회 - 성공")
  void getCreatedMeetings_Success() {
    // given
    Long userId = 1L;
    Long lastId = 0L;

    List<CreatedMeetingProjection> projections = createdMeetingsMockProjections();

    given(meetingRepository.findAllByUser_IdOrderByCreatedAtAsc(
        userId, lastId, TEST_PAGE_SIZE + 1)).willReturn(projections);

    // when
    CreatedMeetingsResponse response =
        meetingService.getCreatedMeetings(userId, lastId, TEST_PAGE_SIZE);

    // then
    List<CreatedMeetingDto> createdMeetingDtos = response.getCreatedMeetingDtos();
    assertThat(createdMeetingDtos).hasSize(TEST_PAGE_SIZE);

    assertThatCreatedMeetingDtos(createdMeetingDtos);
    verify(meetingRepository)
        .findAllByUser_IdOrderByCreatedAtAsc(userId, lastId, TEST_PAGE_SIZE + 1);
  }

  @Test
  @DisplayName("모임 신청자 목록 조회 - 성공")
  void getParticipants_Success() {
    // given
    User user = createUser();
    MeetingCreateRequest request = createMeetingRequest();
    Meeting meeting = createMeeting(user, request);
    List<MeetingParticipantProjection> projections = createMockParticipantProjections();

    given(meetingRepository.findById(meeting.getId())).willReturn(Optional.of(meeting));
    given(participationRepository.findMeetingParticipantsByMeeting_Id(meeting.getId()))
        .willReturn(projections);

    // when
    List<MeetingParticipantProjection> participants =
        meetingService.getParticipants(user.getId(), meeting.getId());

    // then
    assertThat(participants).hasSize(TEST_PAGE_SIZE + 1);

    assertThatMeetingParticipants(participants);
    verify(meetingRepository).findById(meeting.getId());
    verify(participationRepository).findMeetingParticipantsByMeeting_Id(meeting.getId());
  }

  @Test
  @DisplayName("만료된 모임 삭제 - 만료된 모임이 없는 경우")
  void deleteExpiredMeetingsAndNotify_NoExpiredMeetings() {
    // given
    when(meetingRepository.findExpiredMeetings(any(MeetingStatus.class), any(LocalDateTime.class)))
        .thenReturn(Collections.emptyList());
    // when
    meetingService.deleteExpiredMeetingsAndNotify();

    // then
    // 만료된 모임이 없으므로 Repository 메서드가 호출되지 않음을 검증
    verify(chatRoomRepository, never()).deleteAllByMeetingIds(any());
    verify(participationRepository, never()).deleteAllByMeetingIds(any());
    verify(meetingRepository, never()).deleteAllByMeetingIds(any());
  }

  @Test
  @DisplayName("모임 삭제 - 성공")
  void deleteMeeting_Success() {
    // given
    User user = createUser();
    MeetingCreateRequest request = createMeetingRequest();
    Meeting meeting = createMeeting(user, request);
    ChatRoom chatRoom = createChatRoom(user, meeting);

    when(meetingRepository.findById(meeting.getId())).thenReturn(Optional.of(meeting));
    when(chatRoomRepository.findByMeeting_Id(meeting.getId())).thenReturn(Optional.of(chatRoom));

    // when
    meetingService.deleteMeeting(user.getId(), meeting.getId());

    // then
    verify(chatRoomService).deleteRoom(user, chatRoom.getId());
    verify(participationRepository).deleteByMeetingId(meeting.getId());
    verify(meetingRepository).delete(meeting);
    verify(imageService).deleteImage(meeting.getThumbnail());
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
    when(projection.getThumbnail()).thenReturn("test-url" + i + ".jpg");
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
    assertEquals("test-url" + i + ".jpg", meetingDto.getThumbnail());
  }

  private static void verifyCursor(MeetingsResponse response) {
    List<MeetingDto> meetingDtos = response.getMeetings();
    MeetingDto meetingDto = meetingDtos.get(meetingDtos.size() - 1);
    MeetingCursor cursor = response.getCursor();
    assertEquals(cursor.getId(), meetingDto.getId());
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

  private static User createAuthor(Long userId, String email) {
    return User.builder()
        .id(userId)
        .email(email)
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
        .meetingStatus(MeetingStatus.RECRUITING)
        .build();
  }

  private static Meeting createMeeting(User user, MeetingUpdateRequest request) {
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
        .meetingStatus(MeetingStatus.RECRUITING)
        .thumbnail(request.getThumbnail())
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
        .build();
  }

  private MeetingCreateRequest createMeetingInvalidMeetingDateRequest() {
    return MeetingCreateRequest.builder()
        .title("테스트 모임")
        .locationId(123456L)
        .latitude(37.123123)
        .longitude(127.123123)
        .address("테스트 주소")
        .meetingDateTime(LocalDateTime.now().plusYears(1).plusSeconds(1))
        .maxCount(6)
        .category(Set.of(FoodCategory.KOREAN, FoodCategory.JAPANESE))
        .content("테스트 내용")
        .build();
  }

  private static MeetingUpdateRequest createUpdateRequest(String thumbnail) {
    return MeetingUpdateRequest.builder()
        .title("업데이트 테스트 제목")
        .locationId(123456L)
        .latitude(37.123123)
        .longitude(127.123123)
        .address("테스트 주소")
        .meetingDateTime(LocalDateTime.now().plusHours(3))
        .maxCount(5)
        .category(Set.of(FoodCategory.DESSERT))
        .content("업데이트 내용")
        .thumbnail(thumbnail)
        .build();
  }

  private List<CreatedMeetingProjection> createdMeetingsMockProjections() {
    List<CreatedMeetingProjection> projections = new ArrayList<>();

    for (int i = 0; i < TEST_PAGE_SIZE + 1; i++) {
      createdMeetingMockProjection(projections, i);
    }
    return projections;
  }

  private static void createdMeetingMockProjection(
      List<CreatedMeetingProjection> projections, int i
  ) {
    CreatedMeetingProjection projection = mock(CreatedMeetingProjection.class);
    given(projection.getUserId()).willReturn((long) i);
    given(projection.getMeetingId()).willReturn((long) i * 10);
    given(projection.getMeetingStatus()).willReturn(MeetingStatus.RECRUITING);
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

  private static void assertThatCreatedMeetingDtos(List<CreatedMeetingDto> createdMeetingDtos) {
    for (int i = 0; i < createdMeetingDtos.size(); i++) {
      AssertThatAppliedMeeting(createdMeetingDtos, i);
    }
  }

  private static void AssertThatAppliedMeeting(List<CreatedMeetingDto> createdMeetingDto, int i) {
    assertThat(createdMeetingDto.get(i).getUserId()).isEqualTo(i);
    assertThat(createdMeetingDto.get(i).getMeetingId()).isEqualTo(i * 10L);
    assertThat(createdMeetingDto.get(i).getMeetingStatus()).isEqualTo(MeetingStatus.RECRUITING);
    assertThat(createdMeetingDto.get(i).getTitle()).isEqualTo("Test Meeting " + i);
    assertThat(createdMeetingDto.get(i).getLocationId()).isEqualTo(i);
    assertThat(createdMeetingDto.get(i).getLatitude()).isEqualTo(i);
    assertThat(createdMeetingDto.get(i).getLongitude()).isEqualTo(i);
    assertThat(createdMeetingDto.get(i).getAddress()).isEqualTo("Test Address " + i);
    assertThat(createdMeetingDto.get(i).getMeetingDateTime())
        .isEqualTo(LocalDateTime.now().minusHours(1 + i).truncatedTo(ChronoUnit.MINUTES));
    assertThat(createdMeetingDto.get(i).getMaxCount()).isEqualTo(i + 2);
    assertThat(createdMeetingDto.get(i).getApprovedCount()).isEqualTo(i + 1);
    assertThat(createdMeetingDto.get(i).getCategory()).isEqualTo(Set.of("한식", "디저트"));
    assertThat(createdMeetingDto.get(i).getContent()).isEqualTo("Test Content " + i);
    assertThat(createdMeetingDto.get(i).getThumbnail())
        .isEqualTo("test_" + i + "_thumbnail_url.jpg");
  }

  private static List<MeetingParticipantProjection> createMockParticipantProjections() {
    List<MeetingParticipantProjection> projections = new ArrayList<>();

    for (int i = 0; i < TEST_PAGE_SIZE + 1; i++) {
      createMockParticipantProjection(projections, i);
    }
    return projections;
  }

  private static void createMockParticipantProjection(
      List<MeetingParticipantProjection> projections, int i
  ) {
    MeetingParticipantProjection projection = mock(MeetingParticipantProjection.class);
    given(projection.getUserId()).willReturn((long) i);
    given(projection.getNickname()).willReturn("test-nickname" + i);
    given(projection.getProfileImageUrl()).willReturn("test_" + i + "_profile_image_url.jpg");
    given(projection.getParticipationStatus()).willReturn(ParticipationStatus.PENDING);
    projections.add(projection);
  }

  private static void assertThatMeetingParticipants(
      List<MeetingParticipantProjection> projections) {
    for (int i = 0; i < projections.size(); i++) {
      assertThatMeetingParticipant(projections, i);
    }
  }

  private static void assertThatMeetingParticipant(
      List<MeetingParticipantProjection> projections, int i
  ) {
    assertThat(projections.get(i).getUserId()).isEqualTo(i);
    assertThat(projections.get(i).getNickname()).isEqualTo("test-nickname" + i);
    assertThat(projections.get(i).getProfileImageUrl())
        .isEqualTo("test_" + i + "_profile_image_url.jpg");
    assertThat(projections.get(i).getParticipationStatus()).isEqualTo(ParticipationStatus.PENDING);
  }

  private static ChatRoom createChatRoom(User host, Meeting meeting) {
    return ChatRoom.builder()
        .id(1L)
        .host(host)
        .meeting(meeting)
        .reader(new ArrayList<>(List.of(host)))
        .ChatMessages(new ArrayList<>())
        .build();
  }
}