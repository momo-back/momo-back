package com.momo.meeting.service;

import com.momo.chat.service.ChatRoomService;
import com.momo.meeting.constant.MeetingStatus;
import com.momo.meeting.constant.SearchType;
import com.momo.meeting.constant.SortType;
import com.momo.meeting.dto.MeetingCursor;
import com.momo.meeting.dto.MeetingDto;
import com.momo.meeting.dto.createdMeeting.CreatedMeetingsResponse;
import com.momo.meeting.dto.create.MeetingCreateRequest;
import com.momo.meeting.dto.create.MeetingCreateResponse;
import com.momo.meeting.dto.MeetingsRequest;
import com.momo.meeting.dto.MeetingsResponse;
import com.momo.meeting.exception.MeetingErrorCode;
import com.momo.meeting.exception.MeetingException;
import com.momo.meeting.projection.CreatedMeetingProjection;
import com.momo.meeting.projection.MeetingParticipantProjection;
import com.momo.meeting.projection.MeetingToMeetingDtoProjection;
import com.momo.participation.repository.ParticipationRepository;
import com.momo.user.entity.User;
import com.momo.meeting.entity.Meeting;
import com.momo.meeting.repository.MeetingRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeetingService {

  private final MeetingRepository meetingRepository;
  private final ParticipationRepository participationRepository;
  private final ChatRoomService chatRoomService;

  public MeetingCreateResponse createMeeting(User user, MeetingCreateRequest request) {
    validateDailyPostLimit(user.getId());
    Meeting meeting = request.toEntity(request, user);

    meetingRepository.save(meeting);
    chatRoomService.createChatRoom(user, meeting.getId()); // 채팅방 생성

    return MeetingCreateResponse.from(meeting);
  }

  public List<MeetingParticipantProjection> getParticipants(Long userId, Long meetingId) {
    validateForMeetingOwner(userId, meetingId);
    return participationRepository.findMeetingParticipantsByMeeting_Id(meetingId);
  }

  @Transactional
  public MeetingCreateResponse updateMeeting(
      Long userId, Long meetingId, MeetingCreateRequest request
  ) {
    Meeting meeting = validateForMeetingOwner(userId, meetingId);
    meeting.update(request);

    return MeetingCreateResponse.from(meeting);
  }

  @Transactional
  public void updateMeetingStatus(Long userId, Long meetingId, MeetingStatus newStatus) {
    Meeting meeting = validateForMeetingOwner(userId, meetingId);
    meeting.updateStatus(newStatus);
  }

  public void deleteMeeting(Long userId, Long meetingId) {
    Meeting meeting = validateForMeetingOwner(userId, meetingId);
    meetingRepository.delete(meeting);
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

  private Meeting validateForMeetingOwner(Long userId, Long meetingId) {
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
}
