package com.momo.chat.service;

import com.momo.chat.dto.ChatHistoryResponseDto;
import com.momo.chat.dto.ChatRoomResponseDto;
import com.momo.chat.entity.Chat;
import com.momo.chat.entity.ChatRoom;
import com.momo.chat.repository.ChatRepository;
import com.momo.chat.repository.ChatRoomRepository;
import com.momo.chat.validator.ChatValidator;
import com.momo.meeting.entity.Meeting;
import com.momo.profile.entity.Profile;
import com.momo.user.entity.User;
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
  private final ChatValidator chatValidator;

  // 채팅방 생성
  public ChatRoomResponseDto createChatRoom(Long userId, Long meetingId) {
    User user = chatValidator.validateUserExists(userId);
    Meeting meeting = chatValidator.validateMeetingExists(meetingId);

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
    User user = chatValidator.validateUserExists(userId);
    ChatRoom chatRoom = chatValidator.validateChatRoomExists(chatRoomId);

    List<User> readers = chatRoom.getReader();
    readers.add(user);
    chatRoomRepository.save(chatRoom);

    return ChatRoomResponseDto.of(chatRoom);
  }

  // 채팅방 퇴장
  public ChatRoomResponseDto leaveRoom(Long userId, Long chatRoomId) {
    User user = chatValidator.validateUserExists(userId);
    ChatRoom chatRoom = chatValidator.validateChatRoomExists(chatRoomId);

    List<User> readers = chatRoom.getReader();
    readers.remove(user);
    chatRoomRepository.save(chatRoom);

    return ChatRoomResponseDto.of(chatRoom);
  }

  // 참여중인 채팅방 정보 조회
  public ChatRoomResponseDto getRoom(Long userId, Long chatRoomId) {
    ChatRoom chatRoom = chatValidator.validateUserParticipation(userId, chatRoomId);

    return ChatRoomResponseDto.of(chatRoom);
  }

  // 로그인한 유저의 모든 채팅방 목록 조회
  public List<ChatRoomResponseDto> getRooms(Long userId) {
    User user = chatValidator.validateUserExists(userId);

    List<ChatRoom> ChatRooms = chatRoomRepository.findAllByReaderContains(user);

    return ChatRooms.stream()
        .map(chatRoom -> ChatRoomResponseDto.of(chatRoom))
        .collect(Collectors.toList());
  }

  // 참여중인 채팅방 참여자 목록 조회
  public List<Long> getRoomReaders(Long userId, Long chatRoomId) {
    ChatRoom chatRoom = chatValidator.validateUserParticipation(userId, chatRoomId);

    return chatRoom.getReader().stream()
        .map(User::getId)
        .collect(Collectors.toList());
  }

  // 채팅방 삭제 (호스트만 가능)
  public void deleteRoom(Long userId, Long chatRoomId) {
    chatValidator.validateHostOperation(userId, chatRoomId);
    ChatRoom chatRoom = chatValidator.validateChatRoomExists(chatRoomId);

    chatRoomRepository.delete(chatRoom);
  }

  // 특정 사용자 강퇴 (호스트만 가능)
  public ChatRoomResponseDto withdrawal(Long hostUserId, Long chatRoomId, Long targetUserId) {
    chatValidator.validateHostOperation(hostUserId, chatRoomId);
    User targetUser = chatValidator.validateUserExists(targetUserId);
    ChatRoom chatRoom = chatValidator.validateChatRoomExists(chatRoomId);

    chatValidator.validateHostOperation(hostUserId, chatRoomId);

    // 채팅방에서 퇴장 대상 사용자 제거
    chatRoom.getReader().remove(targetUser);
    chatRoomRepository.save(chatRoom);

    return ChatRoomResponseDto.of(chatRoom);
  }

  // 채팅 기록 조회
  public List<ChatHistoryResponseDto> getChatHistory(Long userId, Long chatRoomId) {
    ChatRoom chatRoom = chatValidator.validateUserParticipation(userId, chatRoomId);
    Profile profile = chatValidator.validateProfileExists(userId);

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
