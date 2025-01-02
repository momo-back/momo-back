package com.momo.chat.service;

import com.momo.chat.dto.ChatRoomResponseDto;
import com.momo.chat.entity.ChatRoom;
import com.momo.chat.repository.ChatRepository;
import com.momo.chat.repository.ChatRoomRepository;
import com.momo.meeting.entity.Meeting;
import com.momo.meeting.repository.MeetingRepository;
import com.momo.user.entity.User;
import com.momo.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

  private final ChatRepository chatRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final MeetingRepository meetingRepository;
  private final UserRepository userRepository;

  // 채팅방 생성
  public ChatRoomResponseDto createChatRoom(Long userId, Long meetingId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
    Meeting meeting = meetingRepository.findById(meetingId)
        .orElseThrow(() -> new RuntimeException("해당 모임이 없습니다."));

    List<User> readers = new ArrayList<>();
    readers.add(user);

    ChatRoom chatRoom = ChatRoom.builder()
        .host(user)
        .meeting(meeting)
        .reader(readers)
        .build();

    chatRoomRepository.save(chatRoom);

    return ChatRoomResponseDto.of(chatRoom);

  }

  // 채팅방 입장
  public ChatRoomResponseDto joinRoom(Long userId, Long chatRoomId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
    ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
        .orElseThrow(() -> new RuntimeException("해당 채팅방이 없습니다."));

    List<User> readers = chatRoom.getReader();
    readers.add(user);
    chatRoomRepository.save(chatRoom);

    return ChatRoomResponseDto.of(chatRoom);
  }

  // 채팅방 퇴장
  public ChatRoomResponseDto leaveRoom(Long userId, Long chatRoomId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
    ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
        .orElseThrow(() -> new RuntimeException("해당 채팅방이 없습니다."));

    List<User> readers = chatRoom.getReader();
    readers.remove(user);
    chatRoomRepository.save(chatRoom);

    return ChatRoomResponseDto.of(chatRoom);
  }

  // 참여중인 채팅방 정보 조회
  public ChatRoomResponseDto getRoom(Long userId, Long chatRoomId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
    ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
        .orElseThrow(() -> new RuntimeException("해당 채팅방이 없습니다."));
    if(!chatRoomRepository.existsByReaderContains(user)){
      throw new RuntimeException("해당 채팅방에 참여중이 아닙니다.");
    }

    return ChatRoomResponseDto.of(chatRoom);
  }

  // 로그인한 유저의 모든 채팅방 목록 조회
  public List<ChatRoomResponseDto> getRooms(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

    List<ChatRoom> ChatRooms = chatRoomRepository.findAllByReaderContains(user);

    return ChatRooms.stream()
        .map(chatRoom -> ChatRoomResponseDto.of(chatRoom))
        .collect(Collectors.toList());
  }





}
