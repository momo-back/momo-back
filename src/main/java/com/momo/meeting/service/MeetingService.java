package com.momo.meeting.service;

import com.momo.meeting.dto.MeetingCursor;
import com.momo.meeting.dto.create.MeetingCreateRequest;
import com.momo.meeting.dto.create.MeetingCreateResponse;
import com.momo.meeting.dto.MeetingListReadRequest;
import com.momo.meeting.dto.MeetingListReadResponse;
import com.momo.meeting.dto.MeetingDto;
import com.momo.meeting.exception.MeetingErrorCode;
import com.momo.meeting.exception.MeetingException;
import com.momo.meeting.projection.MeetingToMeetingDtoProjection;
import com.momo.meeting.validation.MeetingValidator;
import com.momo.user.entity.User;
import com.momo.meeting.entity.Meeting;
import com.momo.meeting.repository.MeetingRepository;
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
  private final MeetingValidator meetingValidator;

  public MeetingCreateResponse createMeeting(User user, MeetingCreateRequest request) {
    meetingValidator.validateDailyPostLimit(user.getId());
    Meeting meeting = request.toEntity(request, user);

    Meeting saved = meetingRepository.save(meeting);
    return MeetingCreateResponse.from(saved);
  }

  public MeetingListReadResponse getNearbyMeetings(MeetingListReadRequest request) {
    List<MeetingToMeetingDtoProjection> meetingProjections = getMeetingList(request);

    return MeetingListReadResponse.of(
        MeetingDto.convertToMeetingDtos(meetingProjections),
        createCursor(meetingProjections),
        request.getPageSize());
  }

  @Transactional
  public MeetingCreateResponse updateMeeting(
      Long userId, Long meetingId, MeetingCreateRequest request
  ) {
    Meeting meeting = meetingValidator.validateForMeetingUpdate(userId, meetingId);
    meeting.update(request);

    return MeetingCreateResponse.from(meeting);
  }

  private List<MeetingToMeetingDtoProjection> getMeetingList(
      MeetingListReadRequest request
  ) {
    return meetingRepository.findNearbyMeetingsWithCursor(
        request.getUserLatitude(),
        request.getUserLongitude(),
        request.getRadius(),
        request.getCursorId(),
        request.getCursorDistance(),
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
}
