package com.momo.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatReaderDto {

  private Long id;
  private String nickname;
  private String profileImageUrl;

  public ChatReaderDto() {
  }

  public ChatReaderDto(Long id, String nickname, String profileImageUrl) {
    this.id = id;
    this.nickname = nickname;
    this.profileImageUrl = profileImageUrl;
  }
}
