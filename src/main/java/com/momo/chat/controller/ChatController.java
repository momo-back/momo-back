package com.momo.chat.controller;

import com.momo.chat.dto.ChatRequestDto;
import com.momo.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
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
    System.out.println("받은 메시지: " + dto.getMessage());
    System.out.println("채팅방 ID: " + dto.getRoomId());
    chatService.sendMessage(dto.getUserId(), dto);
  }

  // 채팅방에 입장하면 구독 처리 (여기서는 단순히 방에 메시지를 발송)
  @MessageMapping("/enterRoom")
  public void enterRoom(ChatRequestDto dto) {
    // 채팅방에 입장할 때, 채팅방에 인사 메시지를 전송
    chatService.enterRoom(dto.getUserId(), dto);
  }


}
