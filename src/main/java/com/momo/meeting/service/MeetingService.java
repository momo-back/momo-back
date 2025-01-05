package com.momo.meeting.service;

import com.momo.meeting.constant.SortType;
import com.momo.meeting.dto.MeetingCursor;
import com.momo.meeting.dto.create.MeetingCreateRequest;
import com.momo.meeting.dto.create.MeetingCreateResponse;
import com.momo.meeting.dto.MeetingsRequest;
import com.momo.meeting.dto.MeetingsResponse;
import com.momo.meeting.dto.MeetingDto;
import com.momo.meeting.projection.MeetingToMeetingDtoProjection;
import com.momo.meeting.validation.MeetingValidator;
import com.momo.user.entity.User;
import com.momo.meeting.entity.Meeting;
import com.momo.meeting.repository.MeetingRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingService {

  private final MeetingRepository meetingRepository;
  private final MeetingValidator meetingValidator;

  public MeetingCreateResponse createMeeting(User user, MeetingCreateRequest request) {
    meetingValidator.validateForMeetingCreation(user.getId(), request.getMeetingDateTime());
    Meeting meeting = request.toEntity(request, user);

    Meeting saved = meetingRepository.save(meeting);
    return MeetingCreateResponse.from(saved);
  }

  public MeetingsResponse getMeetings(MeetingsRequest request) {
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

  private List<MeetingToMeetingDtoProjection> getNearbyMeetings(
      MeetingsRequest request
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

  private void logMeetingListInfo(List<MeetingToMeetingDtoProjection> meetingProjections) {
    int i = 1;
    for (MeetingToMeetingDtoProjection meeting : meetingProjections) {
      log.info("{}번째 데이터: ", i++);
      log.info("getId : {}", meeting.getId());
      log.info("getTitle : {}", meeting.getTitle());
      log.info("getLocationId : {}", meeting.getLocationId());
      log.info("getLatitude : {}", meeting.getLatitude());
      log.info("getLongitude : {}", meeting.getLongitude());
      log.info("getAddress : {}", meeting.getAddress());
      log.info("getMeetingDateTime : {}", meeting.getMeetingDateTime());
      log.info("getMaxCount : {}", meeting.getMaxCount());
      log.info("getApprovedCount : {}", meeting.getApprovedCount());
      log.info("getCategory : {}", meeting.getCategory());
      log.info("getThumbnailUrl : {}\n", meeting.getThumbnailUrl());
    }
  }
}
