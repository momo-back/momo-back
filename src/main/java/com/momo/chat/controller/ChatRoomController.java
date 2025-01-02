package com.momo.chat.controller;

import com.momo.chat.dto.ChatRoomResponseDto;
import com.momo.chat.service.ChatRoomService;
import com.momo.user.dto.CustomUserDetails;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chatrooms")
public class ChatRoomController {

  private final ChatRoomService chatRoomService;
  // 채팅방 생성, 입장 시 채팅방 구독, 채팅방 퇴장 시 채팅방 구독 해제 필요
  // 채팅방 생성
  @PostMapping
  public ResponseEntity<ChatRoomResponseDto> createRoom(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @RequestParam Long meetingId
  ) {
    ChatRoomResponseDto roomData = chatRoomService.createChatRoom(
        customUserDetails.getUser().getId(), meetingId);
    return ResponseEntity.ok().body(roomData);
  }

  // 채팅방 입장
  @PostMapping("/join")
  public ResponseEntity<ChatRoomResponseDto> joinRoom(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @RequestParam Long meetingId
  ) {
    ChatRoomResponseDto roomData = chatRoomService.joinRoom(
        customUserDetails.getUser().getId(), meetingId);
    return ResponseEntity.ok().body(roomData);
  }

  // 채팅방 퇴장
  @PostMapping("/leave")
  public ResponseEntity<ChatRoomResponseDto> leaveRoom(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @RequestParam Long meetingId
  ) {
    ChatRoomResponseDto roomData = chatRoomService.leaveRoom(
        customUserDetails.getUser().getId(), meetingId);
    return ResponseEntity.ok().body(roomData);
  }

  // 로그인한 유저의 모든 채팅방 목록 조회
  @GetMapping
  public ResponseEntity<List<ChatRoomResponseDto>> getRooms(
      @AuthenticationPrincipal CustomUserDetails customUserDetails
  ) {
    List<ChatRoomResponseDto> roomData = chatRoomService.getRooms(
        customUserDetails.getUser().getId());
    return ResponseEntity.ok().body(roomData);
  }

  // 채팅방 정보 조회
  @GetMapping
  public ResponseEntity<ChatRoomResponseDto> getRoom(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @RequestParam Long chatRoomId
  ) {
    ChatRoomResponseDto roomData = chatRoomService.getRoom(
        customUserDetails.getUser().getId(), chatRoomId);
    return ResponseEntity.ok().body(roomData);
  }



}
