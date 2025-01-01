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

  @MessageMapping("/chat/message")
  // @SendTo("/sub/chat/room/{roomId}")
  public void sendMessage(ChatRequestDto message) {
    System.out.println("받은 메시지: " + message.getMessage());
    System.out.println("채팅방 ID: " + message.getRoomId());
    chatService.sendMessage(message.getUserId(), message);
  }


}
