package com.momo.meeting.service;

import com.momo.meeting.constant.MeetingStatus;
import com.momo.meeting.constant.SortType;
import com.momo.meeting.dto.create.MeetingCreateRequest;
import com.momo.meeting.dto.create.MeetingCreateResponse;
import com.momo.meeting.dto.MeetingsRequest;
import com.momo.meeting.dto.MeetingsResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  @Transactional
  public void updateMeetingStatus(Long userId, Long meetingId, MeetingStatus newStatus) {
    Meeting meeting = validateForMeetingOwner(userId, meetingId);
    meeting.updateStatus(newStatus);
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

  @Transactional
  public MeetingCreateResponse updateMeeting(
      Long userId, Long meetingId, MeetingCreateRequest request
  ) {
    Meeting meeting = validateForMeetingOwner(userId, meetingId);
    meeting.update(request);

    return MeetingCreateResponse.from(meeting);
  }

  public void deleteMeeting(Long userId, Long meetingId) {
    Meeting meeting = validateForMeetingOwner(userId, meetingId);
    meetingRepository.delete(meeting);
  }

  private List<MeetingToMeetingDtoProjection> getMeetingsByDate(MeetingsRequest request) {
    return meetingRepository.findOrderByMeetingDateWithCursor(
        request.getCursorId(),
        request.getCursorMeetingDateTime(),
        request.getPageSize() + 1 // 다음 페이지 존재 여부를 알기 위해 + 1
    );
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

  private Meeting validateForMeetingOwner(Long userId, Long meetingId) {
    Meeting meeting = meetingRepository.findById(meetingId)
        .orElseThrow(() -> new MeetingException(MeetingErrorCode.MEETING_NOT_FOUND));

    if (!meeting.isOwner(userId)) {
      throw new MeetingException(MeetingErrorCode.NOT_MEETING_OWNER);
    }
    return meeting;
  }
}
