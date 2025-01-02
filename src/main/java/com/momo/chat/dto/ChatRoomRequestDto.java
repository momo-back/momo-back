package com.momo.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRoomRequestDto {

  private Long meetingId;
  private Long roomId;

  public ChatRoomRequestDto() {
  }

  public ChatRoomRequestDto(Long meetingId, Long roomId) {
    this.meetingId = meetingId;
    this.roomId = roomId;
  }
}
