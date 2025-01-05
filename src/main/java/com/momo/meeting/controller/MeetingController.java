package com.momo.meeting.controller;

import com.momo.meeting.dto.create.MeetingCreateRequest;
import com.momo.meeting.dto.create.MeetingCreateResponse;
import com.momo.meeting.dto.MeetingsRequest;
import com.momo.meeting.dto.MeetingsResponse;
import com.momo.meeting.service.MeetingService;
import com.momo.user.dto.CustomUserDetails;
import java.net.URI;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Range;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/meetings")
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
  public ResponseEntity<MeetingsResponse> getMeetings(
      @RequestParam(required = false) @Range(min = -90, max = 90) Double latitude,
      @RequestParam(required = false) @Range(min = -180, max = 180) Double longitude,
      @RequestParam(required = false) Long lastId,
      @RequestParam(required = false) Double lastDistance,
      @RequestParam(defaultValue = "20") @Range(min = 1, max = 100) int pageSize
  ) {
    MeetingsRequest request = MeetingsRequest
        .createRequest(latitude, longitude, lastId, lastDistance, pageSize);
    return ResponseEntity.ok(meetingService.getMeetings(request));
  }

  @PutMapping("/{meetingId}")
  public ResponseEntity<?> updateMeeting(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long meetingId,
      @Valid @RequestBody MeetingCreateRequest request
  ) {
    MeetingCreateResponse response = meetingService.updateMeeting(
        customUserDetails.getId(), meetingId, request);
    return ResponseEntity.ok(response);
  }
}
