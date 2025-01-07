package com.momo.meeting.dto;

import com.momo.meeting.constant.SortType;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MeetingsRequest {

  private static final Long DEFAULT_LAST_ID = 0L;
  private static final double DEFAULT_RADIUS = 3000;
  private static final LocalDateTime DEFAULT_LAST_DATETIME =
      LocalDateTime.of(1970, 1, 1, 0, 0);

  private Double userLatitude;
  private Double userLongitude;
  private Double radius;
  private MeetingCursor meetingCursor;
  private int pageSize;
  private SortType sortType;

  public static MeetingsRequest createRequest(
      Double userLatitude,
      Double userLongitude,
      Long lastId,
      Double lastDistance,
      LocalDateTime lastMeetingDateTime,
      int pageSize
  ) {
    lastId = lastId == null ? DEFAULT_LAST_ID : lastId;
    lastDistance = lastDistance == null ? Double.MIN_VALUE : lastDistance;
    lastMeetingDateTime = lastMeetingDateTime == null ?
        DEFAULT_LAST_DATETIME : lastMeetingDateTime;

    return MeetingsRequest.builder()
        .userLatitude(userLatitude)
        .userLongitude(userLongitude)
        .radius(DEFAULT_RADIUS)
        .meetingCursor(MeetingCursor.of(lastId, lastDistance, lastMeetingDateTime))
        .pageSize(pageSize)
        .sortType(determineSortType(userLatitude, userLongitude))
        .build();
  }

  public Long getCursorId() {
    return meetingCursor.getId();
  }

  public Double getCursorDistance() {
    return meetingCursor.getDistance();
  }

  public LocalDateTime getCursorMeetingDateTime() {
    return meetingCursor.getMeetingDateTime();
  }

  private static SortType determineSortType(Double latitude, Double longitude) {
    return latitude == null || longitude == null ? SortType.DATE : SortType.DISTANCE;
  }
}
