package com.momo.chat.controller;

import com.momo.chat.dto.ChatRoomResponseDto;
import com.momo.chat.service.ChatRoomService;
import com.momo.user.dto.CustomUserDetails;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chatrooms")
public class ChatRoomController {

  private final ChatRoomService chatRoomService;
  // 채팅방 생성, 입장 시 채팅방 구독, 채팅방 삭제, 퇴장, 강퇴시 채팅방 구독 해제 필요
  // 채팅방 생성
  @PostMapping("/{meetingId}")
  public ResponseEntity<ChatRoomResponseDto> createRoom(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long meetingId
  ) {
    ChatRoomResponseDto roomData = chatRoomService.createChatRoom(
        customUserDetails.getUser().getId(), meetingId);
    return ResponseEntity.ok().body(roomData);
  }

  // 채팅방 입장
  @PostMapping("/join/{chatRoomId}")
  public ResponseEntity<ChatRoomResponseDto> joinRoom(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long chatRoomId
  ) {
    ChatRoomResponseDto roomData = chatRoomService.joinRoom(
        customUserDetails.getUser().getId(), chatRoomId);
    return ResponseEntity.ok().body(roomData);
  }

  // 채팅방 퇴장
  @PostMapping("/leave/{chatRoomId}")
  public ResponseEntity<ChatRoomResponseDto> leaveRoom(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long chatRoomId
  ) {
    ChatRoomResponseDto roomData = chatRoomService.leaveRoom(
        customUserDetails.getUser().getId(), chatRoomId);
    return ResponseEntity.ok().body(roomData);
  }

  // 참여중인 채팅방 정보 조회
  @GetMapping("/{chatRoomId}")
  public ResponseEntity<ChatRoomResponseDto> getRoom(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long chatRoomId
  ) {
    ChatRoomResponseDto roomData = chatRoomService.getRoom(
        customUserDetails.getUser().getId(), chatRoomId);
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

  // 참여중인 채팅방 참여자 목록 조회
  @GetMapping("/{chatRoomId}/participants")
  public ResponseEntity<List<Long>> getRoomReaders(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long chatRoomId
  ) {
    List<Long> participants = chatRoomService.getRoomReaders(
        customUserDetails.getUser().getId(), chatRoomId);
    return ResponseEntity.ok().body(participants);
  }

  // 채팅방 삭제 (호스트만 가능)
  @DeleteMapping("/{chatRoomId}")
  public ResponseEntity<Void> deleteRoom(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long chatRoomId
  ) {
    chatRoomService.deleteRoom(customUserDetails.getUser().getId(), chatRoomId);
    return ResponseEntity.noContent().build();
  }

  // 특정 사용자 강퇴 (호스트만 가능)
  @PostMapping("/{chatRoomId}/withdrawal/{userId}")
  public ResponseEntity<ChatRoomResponseDto> withdrawal(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long chatRoomId,
      @PathVariable Long userId
  ) {
    ChatRoomResponseDto roomData = chatRoomService.withdrawal(
        customUserDetails.getUser().getId(), chatRoomId, userId);
    return ResponseEntity.ok().body(roomData);
  }
}
