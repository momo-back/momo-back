package com.momo.meeting.controller;

import com.momo.meeting.constant.FoodCategory;
import com.momo.meeting.constant.MeetingStatus;
import com.momo.meeting.dto.createdMeeting.CreatedMeetingsResponse;
import com.momo.meeting.dto.create.MeetingCreateRequest;
import com.momo.meeting.dto.create.MeetingCreateResponse;
import com.momo.meeting.dto.MeetingsRequest;
import com.momo.meeting.dto.MeetingsResponse;
import com.momo.meeting.service.MeetingService;
import com.momo.user.dto.CustomUserDetails;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Range;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

  /**
   * 모임 생성
   *
   * @param customUserDetails 회원 정보
   * @param request           생성할 모임 정보
   * @return 생성된 모임의 uri, 생성된 모임 정보
   */
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
        .created(URI.create("/api/v1/meetings/" + response.getId()))
        .body(response);
  }

  /**
   * 모집글 목록 조회
   *
   * @param latitude            사용자의 위도
   * @param longitude           사용자의 경도
   * @param lastId              마지막으로 조회된 모임 ID
   * @param lastDistance        마지막으로 조회된 모임 위치 거리
   * @param lastMeetingDateTime 마지막으로 조회된 모임 날짜
   * @param pageSize            조회할 개수
   * @return 조회된 모임 정보, 다음 페이지 여부, 다음 페이지 조회에 사용될 커서
   */
  @GetMapping
  public ResponseEntity<MeetingsResponse> getMeetings(
      @RequestParam(required = false) @Range(min = -90, max = 90) Double latitude,
      @RequestParam(required = false) @Range(min = -180, max = 180) Double longitude,
      @RequestParam(required = false) Long lastId,
      @RequestParam(required = false) Double lastDistance,
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastMeetingDateTime,
      @RequestParam(defaultValue = "20") @Range(min = 1, max = 100) int pageSize
  ) {
    MeetingsRequest request = MeetingsRequest
        .createRequest(latitude, longitude, lastId, lastDistance, lastMeetingDateTime, pageSize);
    return ResponseEntity.ok(meetingService.getMeetings(request));
  }

  /**
   * 주최한 모집글 목록 조회
   *
   * @param customUserDetails 회원 정보
   * @param lastId            마지막으로 조회된 모임 ID
   * @param pageSize          조회할 모임 수
   * @return CreatedMeetingsResponse
   */
  @GetMapping("/created")
  public ResponseEntity<CreatedMeetingsResponse> getCreatedMeetings(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @RequestParam(defaultValue = "0") Long lastId,
      @RequestParam(defaultValue = "20") @Range(min = 1, max = 100) int pageSize
  ) {
    CreatedMeetingsResponse response =
        meetingService.getCreatedMeetings(customUserDetails.getId(), lastId, pageSize);
    return ResponseEntity.ok(response);
  }

  /**
   * 모임 수정
   *
   * @param customUserDetails 회원 정보
   * @param meetingId         수정할 모임 ID
   * @param request           수정할 모임 값
   * @return 수정된 모임의 값
   */
  @PutMapping("/{meetingId}")
  public ResponseEntity<MeetingCreateResponse> updateMeeting(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long meetingId,
      @Valid @RequestBody MeetingCreateRequest request
  ) {
    MeetingCreateResponse response =
        meetingService.updateMeeting(customUserDetails.getId(), meetingId, request);
    return ResponseEntity.ok(response);
  }

  /**
   * 모임 삭제
   *
   * @param customUserDetails 회원 정보
   * @param meetingId         삭제할 모임 ID
   * @return 204 No Content
   */
  @DeleteMapping("/{meetingId}")
  public ResponseEntity<Void> deleteMeeting(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long meetingId
  ) {
    meetingService.deleteMeeting(customUserDetails.getId(), meetingId);
    return ResponseEntity.noContent().build();
  }

  /**
   * 모임 상태 변경
   *
   * @param customUserDetails 회원 정보
   * @param meetingId         상태를 변경할 모임 ID
   * @param meetingStatus     변경할 상태
   * @return 200 OK
   */
  @PatchMapping("/{meetingId}")
  public ResponseEntity<Void> updateMeetingStatus(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long meetingId,
      @RequestBody @NotNull MeetingStatus meetingStatus
  ) {
    meetingService.updateMeetingStatus(customUserDetails.getId(), meetingId, meetingStatus);
    return ResponseEntity.ok().build();
  }

  /**
   * 모집글 목록 검색
   *
   * @param latitude            사용자의 위도
   * @param longitude           사용자의 경도
   * @param lastId              마지막으로 조회된 모임 ID
   * @param lastDistance        마지막으로 조회된 모임 위치 거리
   * @param lastMeetingDateTime 마지막으로 조회된 모임 날짜
   * @param pageSize            조회할 개수
   * @param kind                검색 옵션 ("title", "address", 또는 "content")
   * @param keyword             검색 키워드
   * @param foodCategory        음식 카테고리 필터링 ("", "KOREAN", 또는 "KOREAN,JAPANESE")
   * @return 조회된 모임 정보, 다음 페이지 여부, 다음 페이지 조회에 사용될 커서
   */
  @GetMapping("/search")
  public ResponseEntity<MeetingsResponse> filterMeetings(
      @RequestParam(required = false) @Range(min = -90, max = 90) Double latitude,
      @RequestParam(required = false) @Range(min = -180, max = 180) Double longitude,
      @RequestParam(required = false) Long lastId,
      @RequestParam(required = false) Double lastDistance,
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastMeetingDateTime,
      @RequestParam(defaultValue = "20") @Range(min = 1, max = 100) int pageSize,
      @RequestParam(required = false) String kind,
      @RequestParam(required = false) String keyword,
      @RequestParam String foodCategory
  ) {
    Set<String> categorySet = (foodCategory != null && !foodCategory.isEmpty())
        ? FoodCategory.convertToFoodCategories(foodCategory)
        : null;
    MeetingsRequest request = MeetingsRequest.createRequest(
        latitude, longitude, lastId, lastDistance, lastMeetingDateTime, pageSize
    );
    return ResponseEntity
        .ok(meetingService.filterMeetings(request, kind, keyword, categorySet, pageSize));
  }
}
