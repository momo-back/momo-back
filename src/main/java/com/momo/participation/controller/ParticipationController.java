package com.momo.participation.controller;

import com.momo.participation.service.ParticipationService;
import com.momo.user.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/meetings")
public class ParticipationController {

  private final ParticipationService participationService;

  @PostMapping("/{meetingId}/participations")
  public ResponseEntity<Long> createParticipation(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long meetingId
  ) {
    Long participationId = participationService.createParticipation(
        customUserDetails.getUser(), meetingId
    );
    return ResponseEntity.ok(participationId);
  }
}
