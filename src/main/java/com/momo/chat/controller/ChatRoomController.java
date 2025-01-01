package com.momo.chat.controller;

import com.momo.chat.dto.ChatRoomResponseDto;
import com.momo.chat.service.ChatRoomService;
import com.momo.user.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chatrooms")
public class ChatRoomController {

  private final ChatRoomService chatRoomService;

  // 채팅방 생성
  @PostMapping
  @ResponseBody
  public ResponseEntity<ChatRoomResponseDto> createRoom(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @RequestParam Long meetingId
  ) {
    ChatRoomResponseDto roomData = chatRoomService.createChatRoom(
        customUserDetails.getUser().getId(), meetingId);
    return ResponseEntity.ok().body(roomData);
  }

}
