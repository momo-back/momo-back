package com.momo.chat.service;

import com.momo.chat.dto.ChatHistoryDto;
import com.momo.chat.dto.ChatReaderDto;
import com.momo.chat.dto.ChatRoomDto;
import com.momo.chat.entity.Chat;
import com.momo.chat.entity.ChatRoom;
import com.momo.chat.exception.ChatErrorCode;
import com.momo.chat.exception.ChatException;
import com.momo.chat.repository.ChatRepository;
import com.momo.chat.repository.ChatRoomRepository;
import com.momo.common.exception.CustomException;
import com.momo.common.exception.ErrorCode;
import com.momo.meeting.entity.Meeting;
import com.momo.meeting.exception.MeetingErrorCode;
import com.momo.meeting.exception.MeetingException;
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
  private final UserRepository userRepository;
  private final MeetingRepository meetingRepository;
  private final ProfileRepository profileRepository;

  // 채팅방 생성
  public ChatRoomDto createChatRoom(Long userId, Long meetingId) {
    User user = validateUserExists(userId);
    Meeting meeting = validateMeetingExists(meetingId);

    List<User> readers = new ArrayList<>();
    readers.add(user);

    ChatRoom chatRoom = ChatRoom.builder()
        .host(user)
        .meeting(meeting)
        .reader(readers)
        .build();

    chatRoomRepository.save(chatRoom);

    return ChatRoomDto.of(chatRoom);
  }

  // 채팅방 입장
  public ChatRoomDto joinRoom(Long userId, Long chatRoomId) {
    User user = validateUserExists(userId);
    ChatRoom chatRoom = validateChatRoomExists(chatRoomId);

    List<User> readers = chatRoom.getReader();
    readers.add(user);
    chatRoomRepository.save(chatRoom);

    return ChatRoomDto.of(chatRoom);
  }

  // 채팅방 퇴장
  public ChatRoomDto leaveRoom(Long userId, Long chatRoomId) {
    User user = validateUserExists(userId);
    ChatRoom chatRoom = validateChatRoomExists(chatRoomId);

    List<User> readers = chatRoom.getReader();
    readers.remove(user);
    chatRoomRepository.save(chatRoom);

    return ChatRoomDto.of(chatRoom);
  }

  // 참여중인 채팅방 정보 조회
  public ChatRoomDto getRoom(Long userId, Long chatRoomId) {
    User user = validateUserExists(userId);
    ChatRoom chatRoom = validateChatRoomExists(chatRoomId);

    if (!chatRoom.getReader().contains(user)) {
      throw new ChatException(ChatErrorCode.NOT_A_PARTICIPANT);
    }

    return ChatRoomDto.of(chatRoom);
  }

  // 로그인한 유저의 모든 채팅방 목록 조회
  public List<ChatRoomDto> getRooms(Long userId) {
    User user = validateUserExists(userId);

    List<ChatRoom> ChatRooms = chatRoomRepository.findAllByReaderContains(user);

    return ChatRooms.stream()
        .map(chatRoom -> ChatRoomDto.of(chatRoom))
        .collect(Collectors.toList());
  }

  // 참여중인 채팅방 참여자 목록 조회
  public List<ChatReaderDto> getRoomReaders(Long userId, Long chatRoomId) {
    User user = validateUserExists(userId);
    ChatRoom chatRoom = validateChatRoomExists(chatRoomId);

    if (!chatRoom.getReader().contains(user)) {
      throw new ChatException(ChatErrorCode.NOT_A_PARTICIPANT);
    }

    return chatRoom.getReader().stream()
        .map(reader -> {
          Profile profile = validateProfileExists(reader.getId());
          return new ChatReaderDto(reader.getId(), reader.getNickname(), profile.getProfileImageUrl());
        })
        .collect(Collectors.toList());
  }

  // 채팅방 삭제 (호스트만 가능)
  public void deleteRoom(Long userId, Long chatRoomId) {
    User user = validateUserExists(userId);
    ChatRoom chatRoom = validateChatRoomExists(chatRoomId);

    // 호스트만 채팅방 삭제 가능
    if (!chatRoom.getHost().getId().equals(user.getId())) {
      throw new ChatException(ChatErrorCode.ONLY_HOST_CAN_OPERATE);
    }

    chatRoomRepository.delete(chatRoom);
  }

  // 특정 사용자 강퇴 (호스트만 가능)
  public ChatRoomDto withdrawal(Long hostUserId, Long chatRoomId, Long targetUserId) {
    User hostUser = validateUserExists(hostUserId);
    User targetUser = validateUserExists(targetUserId);
    ChatRoom chatRoom = validateChatRoomExists(chatRoomId);

    // 호스트만 강제 퇴장 가능
    if (!chatRoom.getHost().getId().equals(hostUser.getId())) {
      throw new ChatException(ChatErrorCode.ONLY_HOST_CAN_OPERATE);
    }

    // 채팅방에서 퇴장 대상 사용자 제거
    chatRoom.getReader().remove(targetUser);
    chatRoomRepository.save(chatRoom);

    return ChatRoomDto.of(chatRoom);
  }

  // 채팅 기록 조회
  public List<ChatHistoryDto> getChatHistory(Long userId, Long chatRoomId) {
    User user = validateUserExists(userId);
    ChatRoom chatRoom = validateChatRoomExists(chatRoomId);
    Profile profile = validateProfileExists(userId);

    if (!chatRoom.getReader().contains(user)) {
      throw new ChatException(ChatErrorCode.NOT_A_PARTICIPANT);
    }

    // 채팅방 ID로 모든 채팅 조회
    List<Chat> chats = chatRepository.findAllByChatRoomId(chatRoomId);

    return chats.stream().map(chat -> new ChatHistoryDto(
        chat.getSender().getId(),
        chat.getSender().getNickname(),
        profile.getProfileImageUrl(),
        chat.getMessage(),
        chat.getCreatedAt(),
        chat.getUpdatedAt()
    )).collect(Collectors.toList());
  }

  private User validateUserExists(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
  }

  private Meeting validateMeetingExists(Long meetingId) {
    return meetingRepository.findById(meetingId)
        .orElseThrow(() -> new MeetingException(MeetingErrorCode.MEETING_NOT_FOUND));
  }

  private ChatRoom validateChatRoomExists(Long chatRoomId) {
    return chatRoomRepository.findById(chatRoomId)
        .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
  }

  private Profile validateProfileExists(Long userId) {
    return profileRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_EXISTS_PROFILE));
  }

}