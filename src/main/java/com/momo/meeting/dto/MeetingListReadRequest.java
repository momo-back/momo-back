package com.momo.meeting.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MeetingListReadRequest {

  private static final Long DEFAULT_LAST_ID = 0L;
  private static final double DEFAULT_RADIUS = 3000;

  private double userLatitude;
  private double userLongitude;
  private double radius;
  private MeetingCursor meetingCursor;
  private int pageSize;

  public static MeetingListReadRequest createCursorRequest(
      double userLatitude,
      double userLongitude,
      Long lastId,
      Double lastDistance,
      int pageSize
  ) {
    lastId = lastId == null ? DEFAULT_LAST_ID : lastId;
    lastDistance = lastDistance == null ? Double.MIN_VALUE : lastDistance;

    return MeetingListReadRequest.builder()
        .userLatitude(userLatitude)
        .userLongitude(userLongitude)
        .radius(DEFAULT_RADIUS)
        .meetingCursor(MeetingCursor.of(lastId, lastDistance))
        .pageSize(pageSize)
        .build();
  }

  public Long getCursorId(){
    return meetingCursor.getId();
  }

  public Double getCursorDistance(){
    return meetingCursor.getDistance();
  }
}
