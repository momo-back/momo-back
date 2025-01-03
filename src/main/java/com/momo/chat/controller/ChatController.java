package com.momo.chat.controller;

import com.momo.chat.dto.ChatRequestDto;
import com.momo.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vi")
public class ChatController {

  private final ChatService chatService;

  // 메시지 발송 (사용자가 채팅방에 메시지를 보내는 경우)
  @MessageMapping("/chat/message")
  public void sendMessage(ChatRequestDto dto) {
    chatService.sendMessage(dto.getUserId(), dto);
  }

  // 채팅방에 입장하면 방에 메시지를 발송
  @MessageMapping("/enterRoom")
  public void enterRoom(ChatRequestDto dto) {
    chatService.enterRoom(dto.getUserId(), dto);
  }

  // 채팅방에 퇴장하면 방에 메시지를 발송
  @MessageMapping("/leaveRoom")
  public void leaveRoom(ChatRequestDto dto) {
    chatService.leaveRoom(dto.getUserId(), dto);
  }


}
