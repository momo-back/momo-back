package com.momo.chat.exception;

import lombok.Getter;

@Getter
public class ChatException extends RuntimeException {

  private final ChatErrorCode chatErrorCode;

  public ChatException(ChatErrorCode chatErrorCode) {
    super(chatErrorCode.getMessage());
    this.chatErrorCode = chatErrorCode;
  }
}
