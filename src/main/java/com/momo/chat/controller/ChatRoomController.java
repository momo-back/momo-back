package com.momo.chat.controller;

import com.momo.chat.dto.ChatHistoryDto;
import com.momo.chat.dto.ChatReaderDto;
import com.momo.chat.dto.ChatRoomDto;
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
  // 채팅방 생성 (모임생성)
  @PostMapping("/{meetingId}")
  public ResponseEntity<ChatRoomDto> createRoom(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long meetingId
  ) {
    ChatRoomDto roomData = chatRoomService.createChatRoom(
        customUserDetails.getUser(), meetingId);
    return ResponseEntity.ok().body(roomData);
  }

  // 채팅방 입장 (모임입장)
  @PostMapping("/{roomId}/join")
  public ResponseEntity<ChatRoomDto> joinRoom(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long roomId
  ) {
    ChatRoomDto roomData = chatRoomService.joinRoom(
        customUserDetails.getUser(), roomId);
    return ResponseEntity.ok().body(roomData);
  }

  // 채팅방 퇴장 (모임퇴장)
  @PostMapping("/{roomId}/leave")
  public ResponseEntity<ChatRoomDto> leaveRoom(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long roomId
  ) {
    ChatRoomDto roomData = chatRoomService.leaveRoom(
        customUserDetails.getUser(), roomId);
    return ResponseEntity.ok().body(roomData);
  }

  // 채팅방 들어가기 (채팅 기록 조회)
  @PostMapping("/{roomId}/in")
  public ResponseEntity<List<ChatHistoryDto>> inRoom(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long roomId) {
    List<ChatHistoryDto> history = chatRoomService.getChatHistory(
        customUserDetails.getUser(), roomId);
    return ResponseEntity.ok(history);
  }

  // 채팅방 나가기 (뒤로가기)
  @PostMapping("/{roomId}/out")
  public ResponseEntity<List<ChatRoomDto>> outRoom(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long roomId
  ) {
    List<ChatRoomDto> roomData = chatRoomService.outRoom(
        customUserDetails.getUser(), roomId);
    return ResponseEntity.ok().body(roomData);
  }

  // 참여중인 채팅방 정보 조회
  @GetMapping("/{roomId}")
  public ResponseEntity<ChatRoomDto> getRoom(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long roomId
  ) {
    ChatRoomDto roomData = chatRoomService.getRoom(
        customUserDetails.getUser(), roomId);
    return ResponseEntity.ok().body(roomData);
  }

  // 로그인한 유저의 모든 채팅방 목록 조회
  @GetMapping
  public ResponseEntity<List<ChatRoomDto>> getRooms(
      @AuthenticationPrincipal CustomUserDetails customUserDetails
  ) {
    List<ChatRoomDto> roomData = chatRoomService.getRooms(
        customUserDetails.getUser());
    return ResponseEntity.ok().body(roomData);
  }

  // 참여중인 채팅방 참여자 목록 조회
  @GetMapping("/{roomId}/participants")
  public ResponseEntity<List<ChatReaderDto>> getRoomReaders(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long roomId
  ) {
    List<ChatReaderDto> participants = chatRoomService.getRoomReaders(
        customUserDetails.getUser(), roomId);
    return ResponseEntity.ok().body(participants);
  }

  // 채팅방 삭제 (호스트만 가능)
  @DeleteMapping("/{roomId}")
  public ResponseEntity<Void> deleteRoom(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long roomId
  ) {
    chatRoomService.deleteRoom(customUserDetails.getUser(), roomId);
    return ResponseEntity.noContent().build();
  }

  // 특정 사용자 강퇴 (호스트만 가능)
  @PostMapping("/{roomId}/withdrawal/{userId}")
  public ResponseEntity<ChatRoomDto> withdrawal(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long roomId,
      @PathVariable Long userId
  ) {
    ChatRoomDto roomData = chatRoomService.withdrawal(
        customUserDetails.getUser(), roomId, userId);
    return ResponseEntity.ok().body(roomData);
  }

}
