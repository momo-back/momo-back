package com.momo.chat.service;

import com.momo.chat.dto.ChatHistoryResponseDto;
import com.momo.chat.dto.ChatRoomResponseDto;
import com.momo.chat.entity.Chat;
import com.momo.chat.entity.ChatRoom;
import com.momo.chat.repository.ChatRepository;
import com.momo.chat.repository.ChatRoomRepository;
import com.momo.meeting.entity.Meeting;
import com.momo.meeting.repository.MeetingRepository;
import com.momo.profile.entity.Profile;
import com.momo.profile.repository.ProfileRepository;
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
  private final ProfileRepository profileRepository;

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
    if (!chatRoom.getReader().contains(user)) {
      throw new RuntimeException("해당 채팅방에 참여 중이 아닙니다.");
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

  // 참여중인 채팅방 참여자 목록 조회
  public List<Long> getRoomReaders(Long userId, Long chatRoomId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
    ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
        .orElseThrow(() -> new RuntimeException("해당 채팅방이 없습니다."));

    if (!chatRoom.getReader().contains(user)) {
      throw new RuntimeException("해당 채팅방에 참여 중이 아닙니다.");
    }

    return chatRoom.getReader().stream()
        .map(User::getId)
        .collect(Collectors.toList());
  }

  // 채팅방 삭제 (호스트만 가능)
  public void deleteRoom(Long userId, Long chatRoomId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
    ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
        .orElseThrow(() -> new RuntimeException("해당 채팅방이 없습니다."));

    if (!chatRoom.getHost().getId().equals(user.getId())) {
      throw new RuntimeException("호스트만 채팅방을 삭제할 수 있습니다.");
    }

    chatRoomRepository.delete(chatRoom);
  }

  // 특정 사용자 강퇴 (호스트만 가능)
  public ChatRoomResponseDto withdrawal(Long hostUserId, Long chatRoomId, Long targetUserId) {
    User hostUser = userRepository.findById(hostUserId)
        .orElseThrow(() -> new RuntimeException("호스트 회원을 찾을 수 없습니다."));
    User targetUser = userRepository.findById(targetUserId)
        .orElseThrow(() -> new RuntimeException("강제 퇴장할 사용자를 찾을 수 없습니다."));
    ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
        .orElseThrow(() -> new RuntimeException("해당 채팅방이 없습니다."));

    // 호스트만 강제 퇴장을 시킬 수 있음
    if (!chatRoom.getHost().getId().equals(hostUser.getId())) {
      throw new RuntimeException("호스트만 사용자를 강제 퇴장시킬 수 있습니다.");
    }

    // 강제 퇴장 대상 사용자가 채팅방에 참여 중인지 확인
    if (!chatRoom.getReader().contains(targetUser)) {
      throw new RuntimeException("해당 사용자는 채팅방에 참여 중이 아닙니다.");
    }

    // 채팅방에서 퇴장 대상 사용자 제거
    chatRoom.getReader().remove(targetUser);
    chatRoomRepository.save(chatRoom);

    return ChatRoomResponseDto.of(chatRoom);
  }

  // 채팅 기록 조회
  public List<ChatHistoryResponseDto> getChatHistory(Long userId, Long chatRoomId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
    ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
        .orElseThrow(() -> new RuntimeException("해당 채팅방이 없습니다."));
    Profile profile = profileRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("프로필을 찾을 수 없습니다."));

    if (!chatRoom.getReader().contains(user)) {
      throw new RuntimeException("해당 채팅방에 참여 중이 아닙니다.");
    }

    List<Chat> chats = chatRepository.findAllByChatRoomId(chatRoomId);// 채팅방 ID로 모든 채팅 조회

    return chats.stream().map(chat -> new ChatHistoryResponseDto(
        chat.getSender().getId(),
        chat.getSender().getNickname(),
        profile.getProfileImageUrl(),
        chat.getMessage(),
        chat.getCreatedAt(),
        chat.getUpdatedAt()
    )).collect(Collectors.toList());
  }

}
