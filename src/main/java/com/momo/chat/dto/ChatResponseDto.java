package com.momo.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatResponseDto {
  private Long roomId;
  private String message;
  private String sender;

  public ChatResponseDto() {
  }

  public ChatResponseDto(Long roomId, String message, String sender) {
    this.roomId = roomId;
    this.message = message;
    this.sender = sender;
  }

}
