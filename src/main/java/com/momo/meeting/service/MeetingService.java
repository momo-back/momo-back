package com.momo.meeting.service;

import com.momo.chat.entity.ChatRoom;
import com.momo.chat.exception.ChatErrorCode;
import com.momo.chat.exception.ChatException;
import com.momo.chat.repository.ChatReadStatusRepository;
import com.momo.chat.repository.ChatRepository;
import com.momo.chat.repository.ChatRoomRepository;
import com.momo.chat.service.ChatRoomService;
import com.momo.meeting.constant.MeetingStatus;
import com.momo.meeting.constant.SearchType;
import com.momo.meeting.constant.SortType;
import com.momo.meeting.dto.MeetingCursor;
import com.momo.meeting.dto.MeetingDto;
import com.momo.meeting.dto.MeetingUpdateRequest;
import com.momo.meeting.dto.createdMeeting.CreatedMeetingsResponse;
import com.momo.meeting.dto.MeetingCreateRequest;
import com.momo.meeting.dto.MeetingResponse;
import com.momo.meeting.dto.MeetingsRequest;
import com.momo.meeting.dto.MeetingsResponse;
import com.momo.meeting.exception.MeetingErrorCode;
import com.momo.meeting.exception.MeetingException;
import com.momo.meeting.projection.CreatedMeetingProjection;
import com.momo.meeting.projection.ExpiredMeetingProjection;
import com.momo.meeting.projection.MeetingParticipantProjection;
import com.momo.meeting.projection.MeetingToMeetingDtoProjection;
import com.momo.notification.constant.NotificationType;
import com.momo.notification.service.NotificationService;
import com.momo.participation.constant.ParticipationStatus;
import com.momo.participation.repository.ParticipationRepository;
import com.momo.image.service.ImageService;
import com.momo.user.entity.User;
import com.momo.meeting.entity.Meeting;
import com.momo.meeting.repository.MeetingRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingService {

  @Autowired
  private TransactionTemplate transactionTemplate;

  private final MeetingRepository meetingRepository;
  private final ParticipationRepository participationRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final ChatReadStatusRepository chatReadStatusRepository;
  private final ChatRepository chatRepository;

  private final ChatRoomService chatRoomService;
  private final NotificationService notificationService;
  private final ImageService imageService;

  //@Scheduled(cron = "0/10 * * * * *") // 10초마다 실행(테스트)
  @Scheduled(cron = "0 */5 * * * *") // 5분마다 실행
  public void deleteExpiredMeetingsAndNotify() {
    log.info("Scheduled : 만료된 모임, 채팅방, 참여신청 모두 삭제 시도");

    // 만료된 모임 조회
    List<ExpiredMeetingProjection> expiredMeetings =
        meetingRepository.findExpiredMeetings(MeetingStatus.RECRUITING, LocalDateTime.now());

    if (expiredMeetings.isEmpty()) {
      log.info("No expired meetings found");
      return;
    }

    List<Long> expiredMeetingIds = extractMeetingIds(expiredMeetings); // Meeting ID 목록 추출

    // 승인된 참가자 목록 조회
    List<User> approvedParticipants = findApprovedParticipants(expiredMeetingIds);

    deleteExpiredMeetingData(expiredMeetingIds); // 데이터 삭제
    sendNotifications(expiredMeetings, approvedParticipants); // 알림 발송
  }

  public MeetingResponse createMeeting(
      User user, MeetingCreateRequest request, MultipartFile thumbnail
  ) {
    validateDailyPostLimit(user.getId()); // 하루 포스팅 제한
    validateMeetingDate(request.getMeetingDateTime()); // 날짜 검증 (1년 이내)

    String thumbnailUrl = imageService.uploadImageProcess(thumbnail); // 썸네일 업로드
    Meeting meeting = MeetingCreateRequest.toEntity(request, user, thumbnailUrl);

    meetingRepository.save(meeting);
    chatRoomService.createChatRoom(user, meeting.getId()); // 채팅방 생성

    return MeetingResponse.from(meeting);
  }

  public List<MeetingParticipantProjection> getParticipants(Long userId, Long meetingId) {
    validateForMeetingAuthor(userId, meetingId);
    return participationRepository.findMeetingParticipantsByMeeting_Id(meetingId);
  }

  @Transactional
  public MeetingResponse updateMeeting(
      Long userId, Long meetingId, MeetingUpdateRequest request, MultipartFile newThumbnail
  ) {
    Meeting meeting = validateForMeetingAuthor(userId, meetingId); // 검증
    validateMeetingDate(meeting.getMeetingDateTime()); // 날짜 검증 (1년 이내)

    // 썸네일 처리
    String newThumbnailUrl =
        imageService.handleThumbnailUpdate(meeting.getThumbnail(), newThumbnail);
    meeting.update(request, newThumbnailUrl); // 업데이트

    return MeetingResponse.from(meeting);
  }

  @Transactional
  public void updateMeetingStatus(Long userId, Long meetingId, MeetingStatus newStatus) {
    Meeting meeting = validateForMeetingAuthor(userId, meetingId);
    meeting.updateStatus(newStatus);
  }

  @Transactional
  public void deleteMeeting(Long userId, Long meetingId) {
    Meeting meeting = validateForMeetingAuthor(userId, meetingId);
    ChatRoom chatRoom = chatRoomRepository.findByMeeting_Id(meetingId)
        .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
    
    chatRoomService.deleteRoom(meeting.getUser(), chatRoom.getId()); // 채팅방 삭제
    participationRepository.deleteByMeetingId(meetingId); // 참여신청 삭제
    meetingRepository.delete(meeting); // 모임 삭제
    imageService.deleteImage(meeting.getThumbnail()); // 모임 썸네일 삭제
  }

  public MeetingsResponse getMeetings(MeetingsRequest request) {
    List<MeetingToMeetingDtoProjection> meetingProjections;

    if (request.getSortType() == SortType.DISTANCE) {
      meetingProjections = getNearbyMeetings(request);
    } else {
      meetingProjections = getMeetingsByDate(request);
    }

    return MeetingsResponse.of(
        meetingProjections,
        request.getPageSize()
    );
  }

  public CreatedMeetingsResponse getCreatedMeetings(Long userId, Long lastId, int pageSize) {
    List<CreatedMeetingProjection> createdMeetings =
        meetingRepository.findAllByUser_IdOrderByCreatedAtAsc(userId, lastId, pageSize + 1);
    // 다음 페이지 존재 여부를 알기 위해 + 1

    return CreatedMeetingsResponse.of(
        createdMeetings,
        pageSize
    );
  }

  public MeetingsResponse filterMeetings(
      MeetingsRequest request,
      SearchType searchType,
      String keyword,
      Set<String> categorySet,
      int pageSize
  ) {
    // 기본 모임 목록 조회
    MeetingsResponse response = getMeetings(request);
    Stream<MeetingDto> meetingStream = response.getMeetings().stream();

    // 카테고리 필터링 적용
    if (categorySet != null && !categorySet.isEmpty()) {
      meetingStream = meetingStream.filter(
          meeting -> meeting.getCategory().containsAll(categorySet));
    }

    // 검색 유형 및 키워드 필터링 적용
    if (keyword != null && !keyword.isEmpty()) {
      switch (searchType) {
        case TITLE:
          meetingStream = meetingStream.filter(
              meeting -> meeting.getTitle() != null && meeting.getTitle().contains(keyword));
          break;
        case ADDRESS:
          meetingStream = meetingStream.filter(
              meeting -> meeting.getAddress() != null && meeting.getAddress().contains(keyword));
          break;
        case CONTENT:
          meetingStream = meetingStream.filter(
              meeting -> meeting.getContent() != null && meeting.getContent().contains(keyword));
          break;
        default:
          break; // 유효하지 않은 'kind' 무시
      }
    }

    List<MeetingDto> filteredMeetings = meetingStream.collect(Collectors.toList());
    return processFilteredMeetings(filteredMeetings, pageSize);
  }

  private List<User> findApprovedParticipants(List<Long> expiredMeetingIds) {
    return participationRepository.findParticipantsByMeetingIds(
        expiredMeetingIds,
        ParticipationStatus.APPROVED
    );
  }

  private static List<Long> extractMeetingIds(List<ExpiredMeetingProjection> expiredMeetings) {
    return expiredMeetings.stream()
        .map(ExpiredMeetingProjection::getMeetingId)
        .collect(Collectors.toList());
  }

  private void deleteExpiredMeetingData(List<Long> expiredMeetingIds) {
    // Scheduled의 쓰레드에서는 Transaction을 실행할 수 없기 때문에 직접 실행
    transactionTemplate.execute(status -> {
      chatReadStatusRepository.deleteAllByChatRoomIds(expiredMeetingIds);
      chatRepository.deleteAllByChatRoomIds(expiredMeetingIds);
      int deleteChatRoomCount = chatRoomRepository.deleteAllByMeetingIds(expiredMeetingIds);
      int deleteParticipationCount =
          participationRepository.deleteAllByMeetingIds(expiredMeetingIds);
      int deleteMeetingCount = meetingRepository.deleteAllByMeetingIds(expiredMeetingIds);

      log.info("채팅방 삭제: {}, 참여신청 삭제: {}, 모임 삭제: {}",
          deleteChatRoomCount, deleteParticipationCount, deleteMeetingCount);
      return null;
    });
  }

  private void sendNotifications(
      List<ExpiredMeetingProjection> expiredMeetings,
      List<User> participants
  ) {
    for (ExpiredMeetingProjection expiredMeeting : expiredMeetings) {
      List<User> recipients = new ArrayList<>();
      recipients.add(expiredMeeting.getAuthor());  // 주최자 추가
      recipients.addAll(participants);  // 참가자들 추가

      sendNotificationToExpiredMeetingParticipants(recipients, expiredMeeting.getTitle());
    }
  }

  private void sendNotificationToExpiredMeetingParticipants(List<User> participants, String title) {
    participants.forEach(user -> {
      try {
        notificationService.sendNotification(
            user,
            title + NotificationType.MEETING_EXPIRED.getDescription(),
            NotificationType.MEETING_EXPIRED
        );
        log.info("알림 전송 성공 : user = {}, title = {}", user.getId(), title);
      } catch (Exception e) {
        log.error("알림 전송 실패 : user = {}, title = {}, error = {}",
            user.getId(), title, e.getMessage());
      }
    });
  }

  private void validateDailyPostLimit(Long userId) {
    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);

    int todayPostCount =
        meetingRepository.countByUser_IdAndCreatedAtBetween(userId, startOfDay, endOfDay);

    if (todayPostCount >= 10) {
      throw new MeetingException(MeetingErrorCode.DAILY_POSTING_LIMIT_EXCEEDED);
    }
  }

  private List<MeetingToMeetingDtoProjection> getNearbyMeetings(MeetingsRequest request) {
    return meetingRepository.findNearbyMeetingsWithCursor(
        request.getUserLatitude(),
        request.getUserLongitude(),
        request.getRadius(),
        request.getCursorId(),
        request.getCursorDistance(),
        request.getPageSize() + 1 // 다음 페이지 존재 여부를 알기 위해 + 1
    );
  }

  private List<MeetingToMeetingDtoProjection> getMeetingsByDate(MeetingsRequest request) {
    return meetingRepository.findOrderByMeetingDateWithCursor(
        request.getCursorId(),
        request.getCursorMeetingDateTime(),
        request.getPageSize() + 1 // 다음 페이지 존재 여부를 알기 위해 + 1
    );
  }

  private Meeting validateForMeetingAuthor(Long userId, Long meetingId) {
    Meeting meeting = meetingRepository.findById(meetingId)
        .orElseThrow(() -> new MeetingException(MeetingErrorCode.MEETING_NOT_FOUND));

    if (!meeting.isAuthor(userId)) {
      throw new MeetingException(MeetingErrorCode.NOT_MEETING_OWNER);
    }
    return meeting;
  }

  private MeetingsResponse processFilteredMeetings(
      List<MeetingDto> filteredMeetings, int pageSize
  ) {
    boolean hasNext = filteredMeetings.size() > pageSize;
    filteredMeetings = hasNext ? filteredMeetings.subList(0, pageSize) : filteredMeetings;
    MeetingCursor nextCursor = hasNext ? MeetingCursor.createCursor(filteredMeetings) : null;

    return MeetingsResponse.builder()
        .meetings(filteredMeetings)
        .hasNext(hasNext)
        .cursor(nextCursor)
        .build();
  }

  private static void validateMeetingDate(LocalDateTime meetingDateTime) {
    if (meetingDateTime.isAfter(LocalDateTime.now().plusYears(1))) {
      throw new MeetingException(MeetingErrorCode.INVALID_MEETING_DATE);
    }
  }
}
