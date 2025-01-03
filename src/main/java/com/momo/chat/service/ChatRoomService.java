package com.momo.chat.service;

import com.momo.chat.dto.ChatHistoryResponseDto;
import com.momo.chat.dto.ChatRoomResponseDto;
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
import com.momo.profile.exception.ProfileErrorCode;
import com.momo.profile.exception.ProfileException;
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
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    Meeting meeting = meetingRepository.findById(meetingId)
        .orElseThrow(() -> new MeetingException(MeetingErrorCode.MEETING_NOT_FOUND));

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
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
        .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

    List<User> readers = chatRoom.getReader();
    readers.add(user);
    chatRoomRepository.save(chatRoom);

    return ChatRoomResponseDto.of(chatRoom);
  }

  // 채팅방 퇴장
  public ChatRoomResponseDto leaveRoom(Long userId, Long chatRoomId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
        .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

    List<User> readers = chatRoom.getReader();
    readers.remove(user);
    chatRoomRepository.save(chatRoom);

    return ChatRoomResponseDto.of(chatRoom);
  }

  // 참여중인 채팅방 정보 조회
  public ChatRoomResponseDto getRoom(Long userId, Long chatRoomId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
        .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
    if (!chatRoom.getReader().contains(user)) {
      throw new ChatException(ChatErrorCode.NOT_A_PARTICIPANT);
    }

    return ChatRoomResponseDto.of(chatRoom);
  }

  // 로그인한 유저의 모든 채팅방 목록 조회
  public List<ChatRoomResponseDto> getRooms(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    List<ChatRoom> ChatRooms = chatRoomRepository.findAllByReaderContains(user);

    return ChatRooms.stream()
        .map(chatRoom -> ChatRoomResponseDto.of(chatRoom))
        .collect(Collectors.toList());
  }

  // 참여중인 채팅방 참여자 목록 조회
  public List<Long> getRoomReaders(Long userId, Long chatRoomId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
        .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

    if (!chatRoom.getReader().contains(user)) {
      throw new ChatException(ChatErrorCode.NOT_A_PARTICIPANT);
    }

    return chatRoom.getReader().stream()
        .map(User::getId)
        .collect(Collectors.toList());
  }

  // 채팅방 삭제 (호스트만 가능)
  public void deleteRoom(Long userId, Long chatRoomId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
        .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

    if (!chatRoom.getHost().getId().equals(user.getId())) {
      throw new ChatException(ChatErrorCode.ONLY_HOST_CAN_OPERATE);
    }

    chatRoomRepository.delete(chatRoom);
  }

  // 특정 사용자 강퇴 (호스트만 가능)
  public ChatRoomResponseDto withdrawal(Long hostUserId, Long chatRoomId, Long targetUserId) {
    User hostUser = userRepository.findById(hostUserId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    User targetUser = userRepository.findById(targetUserId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
        .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

    // 호스트만 강제 퇴장을 시킬 수 있음
    if (!chatRoom.getHost().getId().equals(hostUser.getId())) {
      throw new ChatException(ChatErrorCode.ONLY_HOST_CAN_OPERATE);
    }

    // 강제 퇴장 대상 사용자가 채팅방에 참여 중인지 확인
    if (!chatRoom.getReader().contains(targetUser)) {
      throw new ChatException(ChatErrorCode.NOT_A_PARTICIPANT);
    }

    // 채팅방에서 퇴장 대상 사용자 제거
    chatRoom.getReader().remove(targetUser);
    chatRoomRepository.save(chatRoom);

    return ChatRoomResponseDto.of(chatRoom);
  }

  // 채팅 기록 조회
  public List<ChatHistoryResponseDto> getChatHistory(Long userId, Long chatRoomId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
        .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
    Profile profile = profileRepository.findById(userId)
        .orElseThrow(() -> new ProfileException(ProfileErrorCode.INVALID_IMAGE_FORMAT));

    if (!chatRoom.getReader().contains(user)) {
      throw new ChatException(ChatErrorCode.NOT_A_PARTICIPANT);
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
