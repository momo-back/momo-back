package com.momo.participation.controller;

import com.momo.participation.constant.ParticipationStatus;
import com.momo.participation.dto.AppliedMeetingsResponse;
import com.momo.participation.service.ParticipationService;
import com.momo.user.dto.CustomUserDetails;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Range;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/participations")
public class ParticipationController {

  private final ParticipationService participationService;

  /**
   * 모임 참여 신청
   *
   * @param customUserDetails 회원 정보
   * @param meetingId         참여 신청할 모임 ID
   */
  @PostMapping("/{meetingId}")
  public ResponseEntity<Void> createParticipation(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long meetingId
  ) {
    participationService.createParticipation(customUserDetails.getUser(), meetingId);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .build();
  }

  /**
   * 참여한 모임 목록 조회
   *
   * @param customUserDetails 회원 정보
   * @param lastId            마지막으로 조회된 모임 ID
   * @param pageSize          조회할 개수
   * @return 조회된 모임, 마지막으로 조회된 모임 ID, 다음 페이지 여부
   */
  @GetMapping
  public ResponseEntity<AppliedMeetingsResponse> getAppliedMeetings(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @RequestParam(defaultValue = "0") Long lastId,
      @RequestParam(defaultValue = "20") @Range(min = 1, max = 100) int pageSize
  ) {
    AppliedMeetingsResponse response =
        participationService.getAppliedMeetings(customUserDetails.getId(), lastId, pageSize);

    return ResponseEntity.ok(response);
  }

  // TODO: 참여 승인과 참여 거절로 분리
  @PatchMapping("/{participationId}")
  public ResponseEntity<Void> updateParticipationStatus(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long participationId,
      @RequestBody @NotNull ParticipationStatus participationStatus
  ) {
    participationService.updateParticipationStatus(
        customUserDetails.getId(), participationId, participationStatus
    );
    return ResponseEntity.ok()
        .build();
  }

  /**
   * 모임 참여 신청 취소
   *
   * @param customUserDetails 회원 정보
   * @param participationId   참여 신청 ID
   */
  @DeleteMapping("/{participationId}/cancel")
  public ResponseEntity<Void> cancelParticipation(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long participationId
  ) {
    participationService.cancelParticipation(customUserDetails.getId(), participationId);
    return ResponseEntity.noContent().build();
  }
}
