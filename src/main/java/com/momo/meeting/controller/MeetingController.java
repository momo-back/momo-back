package com.momo.meeting.controller;

import com.momo.common.annotation.RequireProfile;
import com.momo.meeting.dto.MeetingCreateRequest;
import com.momo.meeting.dto.MeetingCreateResponse;
import com.momo.meeting.service.MeetingService;
import com.momo.user.dto.CustomUserDetails;
import java.net.URI;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
        .created(URI.create("api/posts/" + response.getId()))
        .body(response);
  }
}
