package com.momo.config.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;

@Getter
@RequiredArgsConstructor
public enum ValidatePath {
  // 프로필 생성 검증 경로가 생기면 여기에 추가

  // 모임
  MEETINGS("/api/v1/meetings/**", HttpMethod.POST), // 모든 POST 요청
  MEETINGS_CREATED("/api/v1/meetings/created", HttpMethod.GET), // 주최한 모집글 조회
  MEETINGS_PARTICIPATION("/api/v1/meetings/*/participants", HttpMethod.GET), // 신청자 목록 조회
  MEETINGS_UPDATE("/api/v1/meetings/**", HttpMethod.PUT), // 모든 UPDATE 요청
  MEETINGS_DELETE("/api/v1/meetings/**", HttpMethod.DELETE), // 모든 DELETE 요청
  MEETINGS_PATCH("/api/v1/meetings/**", HttpMethod.PATCH), // 모든 PATCH 요청

  // 참여 신청
  PARTICIPATION_CREATE("/api/v1/participations/**", HttpMethod.POST), // 모든 POST 요청
  APPLIED_MEETINGS("/api/v1/participations", HttpMethod.GET), // 신청한 모임 목록 조회
  APPROVED_PARTICIPATION("/api/v1/participations/*/reject", HttpMethod.PATCH), // 참여 신청 승인
  REJECT_PARTICIPATION("/api/v1/participations/*/approve", HttpMethod.PATCH), // 참여 신청 거절
  PARTICIPATION_DELETE("/api/v1/participations/**", HttpMethod.DELETE), // 모든 DELETE 요청

  // 채팅
  CHAT_CREATE("/api/v1/chats/**", HttpMethod.POST), // 모든 POST 요청
  CHAT_GET("/api/v1/chats/**", HttpMethod.GET), // 모든 GET 요청
  CHAT_DELETE("/api/v1/chats/**", HttpMethod.DELETE), // 모든 DELETE 요청
  ;

  private final String path;
  private final HttpMethod method;
}
