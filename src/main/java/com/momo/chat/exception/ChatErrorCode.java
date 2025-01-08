package com.momo.chat.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ChatErrorCode {

  CHAT_ROOM_NOT_FOUND("해당 채팅방이 없습니다.", HttpStatus.NOT_FOUND),
  NOT_A_PARTICIPANT("해당 채팅방에 참여 중이 아닙니다.", HttpStatus.FORBIDDEN),
  ALREADY_A_PARTICIPANT("이미 채팅방의 참여자입니다.", HttpStatus.BAD_REQUEST),
  ONLY_HOST_CAN_OPERATE("호스트만 이 작업을 수행할 수 있습니다.", HttpStatus.UNAUTHORIZED),
  READ_STATUS_NOT_FOUND("읽기 상태 정보가 없습니다.", HttpStatus.NOT_FOUND);

  private final String message;
  private final HttpStatus status;
}
