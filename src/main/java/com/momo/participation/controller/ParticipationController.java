package com.momo.participation.controller;

import com.momo.participation.dto.AppliedMeetingsResponse;
import com.momo.participation.service.ParticipationService;
import com.momo.user.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Range;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/participations")
public class ParticipationController {

  private final ParticipationService participationService;

  @PostMapping("/{meetingId}")
  public ResponseEntity<Long> createParticipation(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long meetingId
  ) {
    Long participationId = participationService.createParticipation(
        customUserDetails.getUser(), meetingId
    );
    return ResponseEntity.ok(participationId);
  }

  @GetMapping
  public ResponseEntity<AppliedMeetingsResponse> getAppliedMeetings(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @RequestParam(defaultValue = "0") Long lastId,
      @RequestParam(defaultValue = "20") @Range(min = 1, max = 100) int pageSize
  ) {
    AppliedMeetingsResponse response = participationService.getAppliedMeetings(
        customUserDetails.getId(), lastId, pageSize
    );
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{participationId}/cancel")
  public ResponseEntity<Void> cancelParticipation(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long participationId
  ) {
    participationService.cancelParticipation(customUserDetails.getId(), participationId);
    return ResponseEntity.noContent().build();
  }
}
