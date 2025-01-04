package com.momo.meeting.controller;

import com.momo.meeting.dto.create.MeetingCreateRequest;
import com.momo.meeting.dto.create.MeetingCreateResponse;
import com.momo.meeting.dto.MeetingListReadRequest;
import com.momo.meeting.dto.MeetingListReadResponse;
import com.momo.meeting.service.MeetingService;
import com.momo.user.dto.CustomUserDetails;
import java.net.URI;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Range;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/meetings")
@RequiredArgsConstructor
public class MeetingController {

  private final MeetingService meetingService;

  @PostMapping
  public ResponseEntity<MeetingCreateResponse> createMeeting(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @Valid @RequestBody MeetingCreateRequest request
  ) {
    MeetingCreateResponse response = meetingService.createMeeting(
        customUserDetails.getUser(),
        request
    );
    return ResponseEntity
        .created(URI.create("/api/meetings/" + response.getId()))
        .body(response);
  }

  @GetMapping
  public ResponseEntity<MeetingListReadResponse> getNearbyMeetings(
      @RequestParam @Range(min = -90, max = 90) double latitude,
      @RequestParam @Range(min = -180, max = 180) double longitude,
      @RequestParam(required = false) Long lastId,
      @RequestParam(required = false) Double lastDistance,
      @RequestParam(defaultValue = "20") @Range(min = 1, max = 100) int pageSize
  ) {
    MeetingListReadRequest request = MeetingListReadRequest
        .createCursorRequest(latitude, longitude, lastId, lastDistance, pageSize);
    return ResponseEntity.ok(meetingService.getNearbyMeetings(request));
  }
}
