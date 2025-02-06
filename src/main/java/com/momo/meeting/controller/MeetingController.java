package com.momo.meeting.controller;

import com.momo.meeting.constant.FoodCategory;
import com.momo.meeting.constant.SearchType;
import com.momo.meeting.dto.MeetingUpdateRequest;
import com.momo.meeting.dto.MeetingStatusRequest;
import com.momo.meeting.dto.createdMeeting.CreatedMeetingsResponse;
import com.momo.meeting.dto.MeetingCreateRequest;
import com.momo.meeting.dto.MeetingResponse;
import com.momo.meeting.dto.MeetingsRequest;
import com.momo.meeting.dto.MeetingsResponse;
import com.momo.meeting.projection.MeetingParticipantProjection;
import com.momo.meeting.service.MeetingService;
import com.momo.user.dto.CustomUserDetails;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
  public ResponseEntity<MeetingResponse> createMeeting(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @Valid @RequestPart MeetingCreateRequest request,
      @RequestPart(required = false) MultipartFile thumbnail
  ) {
    MeetingResponse response = meetingService.createMeeting(
        customUserDetails.getUser(),
        request,
        thumbnail
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
   * 모임 참여 신청자 목록 조회
   *
   * @param customUserDetails 회원 정보
   * @param meetingId         모임 ID
   * @return 참여 신정자의 ID, 닉네임, 프로필 사진 URL, 참여 상태를 담은 List 반환
   */
  @GetMapping("/{meetingId}/participants")
  public ResponseEntity<List<MeetingParticipantProjection>> getMeetingParticipant(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long meetingId
  ) {
    List<MeetingParticipantProjection> response =
        meetingService.getParticipants(customUserDetails.getId(), meetingId);

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
  public ResponseEntity<MeetingResponse> updateMeeting(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long meetingId,
      @Valid @RequestPart MeetingUpdateRequest request,
      @RequestPart(required = false) MultipartFile newThumbnail
  ) {
    MeetingResponse response =
        meetingService.updateMeeting(customUserDetails.getId(), meetingId, request, newThumbnail);
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
   * 모집 완료
   */
  @PatchMapping("/{meetingId}/complete")
  public ResponseEntity<Void> completedMeeting(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long meetingId
  ) {
    meetingService.completedMeeting(customUserDetails.getId(), meetingId);
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
   * @param searchType          검색 옵션 (TITLE, ADDRESS, 또는 CONTENT)
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
      @RequestParam(required = false) SearchType searchType,
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
        .ok(meetingService.filterMeetings(request, searchType, keyword, categorySet, pageSize));
  }
}
