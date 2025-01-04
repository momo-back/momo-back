package com.momo.chat.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatHistoryResponseDto {
  private Long userId;
  private String userNickname;
  private String userProfileImageUrl;
  private String message;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public ChatHistoryResponseDto(Long userId, String userNickname, String userProfileImageUrl,
      String message, LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.userId = userId;
    this.userNickname = userNickname;
    this.userProfileImageUrl = userProfileImageUrl;
    this.message = message;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }
}
