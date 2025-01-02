package com.momo.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequestDto {
  private Long userId;
  private Long roomId;
  private String message;

  public ChatRequestDto() {
  }

  public ChatRequestDto(Long userId, Long roomId, String message) {
    this.userId = userId;
    this.roomId = roomId;
    this.message = message;
  }
}
