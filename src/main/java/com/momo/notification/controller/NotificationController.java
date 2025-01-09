package com.momo.notification.controller;

import com.momo.notification.dto.NotificationResponse;
import com.momo.notification.service.NotificationService;
import com.momo.user.dto.CustomUserDetails;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

  private final NotificationService notificationService;


  /**
   * SSE 연결 설정 엔드포인트
   *
   * @param customUserDetails 회원 정보
   * @return 단방향 통신 시작
   */
  @GetMapping(value = "subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
    return notificationService.subscribeSseEmitter(customUserDetails.getId());
  }

  /**
   * 알림 목록 조회
   *
   * @param customUserDetails 회원 정보
   * @return 알림 목록
   */
  // TODO: 정렬기준 정하고 커서 기반으로 수정
  @GetMapping
  public ResponseEntity<List<NotificationResponse>> getNotifications(
      @AuthenticationPrincipal CustomUserDetails customUserDetails
  ) {
    List<NotificationResponse> notifications =
        notificationService.getNotifications(customUserDetails.getId());
    return ResponseEntity.ok(notifications);
  }

  /**
   * 특정 알림 삭제
   *
   * @param customUserDetails 회원 정보
   * @param notificationId    삭제할 알림 ID
   * @return 204 No Content
   */
  @DeleteMapping("/{notificationId}")
  public ResponseEntity<Void> deleteNotification(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long notificationId
  ) {
    notificationService.deleteNotification(customUserDetails.getId(), notificationId);
    return ResponseEntity.noContent().build();
  }

  /**
   * 전체 알림 삭제
   *
   * @param customUserDetails 회원 정보
   * @return 204 No Content
   */
  @DeleteMapping
  public ResponseEntity<Void> deleteAllNotifications(
      @AuthenticationPrincipal CustomUserDetails customUserDetails
  ) {
    notificationService.deleteAllNotifications(customUserDetails.getId());
    return ResponseEntity.noContent().build();
  }
}
