package com.momo.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatResponseDto {
  private Long roomId;
  private String message;
  private String senderNickname;
  private Long senderId;
  private String userProfileImageUrl;

  public ChatResponseDto() {
  }

  public ChatResponseDto(Long roomId, String message, String senderNickname, Long senderId, String userProfileImageUrl) {
    this.roomId = roomId;
    this.message = message;
    this.senderNickname = senderNickname;
    this.senderId = senderId;
    this.userProfileImageUrl = userProfileImageUrl;
  }

}
