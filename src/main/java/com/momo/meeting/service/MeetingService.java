package com.momo.meeting.service;

import com.momo.meeting.constant.MeetingStatus;
import com.momo.meeting.constant.SortType;
import com.momo.meeting.dto.MeetingCursor;
import com.momo.meeting.dto.create.MeetingCreateRequest;
import com.momo.meeting.dto.create.MeetingCreateResponse;
import com.momo.meeting.dto.MeetingsRequest;
import com.momo.meeting.dto.MeetingsResponse;
import com.momo.meeting.dto.MeetingDto;
import com.momo.meeting.exception.MeetingErrorCode;
import com.momo.meeting.exception.MeetingException;
import com.momo.meeting.projection.MeetingToMeetingDtoProjection;
import com.momo.user.entity.User;
import com.momo.meeting.entity.Meeting;
import com.momo.meeting.repository.MeetingRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingService {

  private final MeetingRepository meetingRepository;

  public MeetingCreateResponse createMeeting(User user, MeetingCreateRequest request) {
    validateDailyPostLimit(user.getId());
    Meeting meeting = request.toEntity(request, user);

    meetingRepository.save(meeting);
    return MeetingCreateResponse.from(meeting);
  }

  public MeetingsResponse getMeetings(MeetingsRequest request) {
// TODO: 각 모집글 주최자 아이디 같이 반환 필요.
    List<MeetingToMeetingDtoProjection> meetingProjections;

    if (request.getSortType() == SortType.DISTANCE) {
      meetingProjections = getNearbyMeetings(request);
    } else {
      meetingProjections = getMeetingsByDate(request);
    }

    return MeetingsResponse.of(
        MeetingDto.convertToMeetingDtos(meetingProjections),
        createCursor(meetingProjections),
        request.getPageSize()
    );
  }

  @Transactional
  public void updateMeetingStatus(Long userId, Long meetingId, MeetingStatus newStatus) {
    Meeting meeting = meetingRepository.findById(meetingId)
        .orElseThrow(() -> new MeetingException(MeetingErrorCode.MEETING_NOT_FOUND));
    /*if (!meeting.isOwner(userId)) {
      // TODO: merge 후 구현
    }*/
    meeting.updateStatus(newStatus);
  }

  public List<MeetingToMeetingDtoProjection> getNearbyMeetings(MeetingsRequest request) {
    return meetingRepository.findNearbyMeetingsWithCursor(
        request.getUserLatitude(),
        request.getUserLongitude(),
        request.getRadius(),
        request.getCursorId(),
        request.getCursorDistance(),
        request.getPageSize() + 1 // 다음 페이지 존재 여부를 알기 위해 + 1
    );
  }

  @Transactional
  public MeetingCreateResponse updateMeeting(
      Long userId, Long meetingId, MeetingCreateRequest request
  ) {
    Meeting meeting = validateForMeetingUpdate(userId, meetingId);
    meeting.update(request);

    return MeetingCreateResponse.from(meeting);
  }

  private List<MeetingToMeetingDtoProjection> getMeetingsByDate(
      MeetingsRequest request
  ) {
    return meetingRepository.findOrderByCreatedAtWithCursor(
        request.getCursorId(),
        request.getPageSize() + 1 // 다음 페이지 존재 여부를 알기 위해 + 1
    );
  }

  private MeetingCursor createCursor(List<MeetingToMeetingDtoProjection> meetingProjections) {
    if (meetingProjections.isEmpty()) {
      return null;
    }
    MeetingToMeetingDtoProjection lastProjection = meetingProjections
        .get(meetingProjections.size() - 1);
    return MeetingCursor.of(lastProjection.getId(), lastProjection.getDistance());
  }

  private void validateDailyPostLimit(Long userId) {
    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);

    int todayPostCount = meetingRepository.countByUser_IdAndCreatedAtBetween(
        userId, startOfDay, endOfDay
    );
    if (todayPostCount >= 10) {
      throw new MeetingException(MeetingErrorCode.DAILY_POSTING_LIMIT_EXCEEDED);
    }
  }

  private Meeting validateForMeetingUpdate(Long userId, Long meetingId) {
    Meeting meeting = meetingRepository.findById(meetingId)
        .orElseThrow(() -> new MeetingException(MeetingErrorCode.MEETING_NOT_FOUND));

    if (!meeting.isOwner(userId)) {
      throw new MeetingException(MeetingErrorCode.NOT_MEETING_OWNER);
    }
    return meeting;
  }
}
