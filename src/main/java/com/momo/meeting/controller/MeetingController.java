package com.momo.meeting.controller;

import com.momo.meeting.dto.CreateMeetingRequest;
import com.momo.meeting.service.MeetingService;
import java.net.URI;
import java.nio.file.attribute.UserPrincipal;
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
  public ResponseEntity<Long> createMeeting(
      @AuthenticationPrincipal() UserPrincipal userPrincipal,
      @Valid @RequestBody CreateMeetingRequest request
  ) {
    Long meetingId = meetingService.createMeeting(request, 1L); // TODO: 회원의 ID로 변경
    return ResponseEntity
        .created(URI.create("api/posts/" + meetingId))
        .body(meetingId);
  }
}
