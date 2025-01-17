package com.momo.chat.service;

import com.momo.chat.dto.ChatHistoryDto;
import com.momo.chat.dto.ChatReaderDto;
import com.momo.chat.dto.ChatRoomDto;
import com.momo.chat.entity.Chat;
import com.momo.chat.entity.ChatReadStatus;
import com.momo.chat.entity.ChatRoom;
import com.momo.chat.exception.ChatErrorCode;
import com.momo.chat.exception.ChatException;
import com.momo.chat.repository.ChatReadStatusRepository;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

  private final SimpMessageSendingOperations messagingTemplate;
  private final ChatRepository chatRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final ChatReadStatusRepository chatReadStatusRepository;
  private final UserRepository userRepository;
  private final MeetingRepository meetingRepository;
  private final ProfileRepository profileRepository;

  // 채팅방 생성 (모임생성)
  @Transactional
  public ChatRoomDto createChatRoom(User user, Long meetingId) {
    Meeting meeting = validateMeetingExists(meetingId);

    List<User> readers = new ArrayList<>();
    readers.add(user);

    ChatRoom chatRoom = ChatRoom.builder()
        .host(user)
        .meeting(meeting)
        .reader(readers)
        .build();

    ChatReadStatus chatReadStatus = ChatReadStatus.builder()
        .user(user)
        .chatRoom(chatRoom)
        .lastReadAt(LocalDateTime.now())
        .build();

    chatRoomRepository.save(chatRoom);
    chatReadStatusRepository.save(chatReadStatus);

    return ChatRoomDto.of(chatRoom);
  }

  // 채팅방 입장 (모임입장)
  @Transactional
  public ChatRoomDto joinRoom(User user, Long chatRoomId) {
    ChatRoom chatRoom = validateChatRoomExists(chatRoomId);

    if(chatRoom.getReader().contains(user)) {
      throw new ChatException(ChatErrorCode.ALREADY_A_PARTICIPANT);
    }

    List<User> readers = chatRoom.getReader();
    readers.add(user);

    ChatReadStatus chatReadStatus = ChatReadStatus.builder()
        .user(user)
        .chatRoom(chatRoom)
        .lastReadAt(LocalDateTime.now())
        .build();

    chatRoomRepository.save(chatRoom);
    chatReadStatusRepository.save(chatReadStatus);

    // 입장하면 채팅방에 메시지를 발송
    messagingTemplate.convertAndSend("/sub/chat/room/" + chatRoomId,
        user.getNickname() + "님이 입장했습니다.");

    return ChatRoomDto.of(chatRoom);
  }

  // 채팅방 퇴장 (모임퇴장)
  @Transactional
  public ChatRoomDto leaveRoom(User user, Long chatRoomId) {
    ChatRoom chatRoom = validateChatRoomExists(chatRoomId);

    List<User> readers = chatRoom.getReader();
    readers.remove(user);
    chatRoomRepository.save(chatRoom);
    chatReadStatusRepository.deleteByUserIdAndChatRoomId(user.getId(), chatRoomId);

    // 퇴장하면 채팅방에 메시지를 발송
    messagingTemplate.convertAndSend("/sub/chat/room/" + chatRoomId,
        user.getNickname() + "님이 퇴장했습니다.");

    // TODO: 채팅방 퇴장시 모임퇴장 되도록

    return ChatRoomDto.of(chatRoom);
  }

  // 채팅방 들어가기 (채팅 기록 조회)
  @Transactional
  public List<ChatHistoryDto> getChatHistory(User user, Long chatRoomId) {
    ChatRoom chatRoom = validateChatRoomExists(chatRoomId);
    Profile profile = validateProfileExists(user.getId());
    ChatReadStatus chatReadStatus = validateChatReadStatus(user.getId(), chatRoomId);

    if (!chatRoom.getReader().contains(user)) {
      throw new ChatException(ChatErrorCode.NOT_A_PARTICIPANT);
    }

    chatReadStatus.setLastReadAt(LocalDateTime.now());
    chatReadStatusRepository.save(chatReadStatus);

    List<ChatRoomDto> rooms = getRooms(user);
    // 해당 방의 unreadMessagesCount를 0으로 설정
    setUnreadMessagesCountToZero(chatRoomId, rooms);

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

  // 채팅방 나가기 (뒤로가기)
  @Transactional
  public List<ChatRoomDto> outRoom(User user, Long chatRoomId) {
    ChatRoom chatRoom = validateChatRoomExists(chatRoomId);
    ChatReadStatus chatReadStatus = validateChatReadStatus(user.getId(), chatRoomId);

    if (!chatRoom.getReader().contains(user)) {
      throw new ChatException(ChatErrorCode.NOT_A_PARTICIPANT);
    }

    chatReadStatus.setLastReadAt(LocalDateTime.now());
    chatReadStatusRepository.save(chatReadStatus);

    List<ChatRoomDto> rooms = getRooms(user);
    // 해당 방의 unreadMessagesCount를 0으로 설정
    setUnreadMessagesCountToZero(chatRoomId, rooms);

    return rooms;
  }

  // 참여중인 채팅방 정보 조회
  @Transactional
  public ChatRoomDto getRoom(User user, Long chatRoomId) {
    ChatRoom chatRoom = validateChatRoomExists(chatRoomId);

    if (!chatRoom.getReader().contains(user)) {
      throw new ChatException(ChatErrorCode.NOT_A_PARTICIPANT);
    }

    return ChatRoomDto.of(chatRoom);
  }

  // 로그인한 유저의 모든 채팅방 목록 조회
  @Transactional
  public List<ChatRoomDto> getRooms(User user) {
    List<ChatRoom> chatRooms = chatRoomRepository.findAllByReaderContains(user);

    return chatRooms.stream()
        .map(chatRoom -> {
          // 데이터베이스에서 마지막으로 읽은 시간을 가져오거나, 사용가능한 데이터가 없으면 최소 시간을 사용
          LocalDateTime lastReadAt = chatReadStatusRepository.findByUserIdAndChatRoomId(chatRoom.getId(), user.getId())
              .map(ChatReadStatus::getLastReadAt)
              .orElse(LocalDateTime.MIN);

          // 각 채팅방에 대한 읽지 않은 메시지 수를 계산
          Integer unreadMessagesCount = (int) chatRoom.getChatMessages().stream()
              .filter(chat -> chat.getCreatedAt().isAfter(lastReadAt))
              .count();

          // 참여자 ID 추출
          List<Long> readerIds = chatRoom.getReader().stream()
              .map(User::getId)
              .collect(Collectors.toList());

          // 존재하는 경우 마지막 메시지 결정
          String lastMessage = chatRoom.getChatMessages().isEmpty() ? "" :
              chatRoom.getChatMessages().get(chatRoom.getChatMessages().size() - 1).getMessage();

          // 필요한 데이터를 포함하여 ChatRoomDto 객체 생성 및 반환
          return new ChatRoomDto(
              chatRoom.getId(),
              chatRoom.getMeeting().getId(),
              chatRoom.getMeeting().getThumbnailUrl(),
              chatRoom.getMeeting().getTitle(),
              lastMessage,
              chatRoom.getHost().getId(),
              readerIds,
              unreadMessagesCount
          );
        })
        .collect(Collectors.toList());
  }

  // 참여중인 채팅방 참여자 목록 조회
  @Transactional
  public List<ChatReaderDto> getRoomReaders(User user, Long chatRoomId) {
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
  @Transactional
  public void deleteRoom(User user, Long chatRoomId) {
    ChatRoom chatRoom = validateChatRoomExists(chatRoomId);

    // 호스트만 채팅방 삭제 가능
    if (!chatRoom.getHost().getId().equals(user.getId())) {
      throw new ChatException(ChatErrorCode.ONLY_HOST_CAN_OPERATE);
    }

    chatReadStatusRepository.deleteByChatRoomId(chatRoomId);
    chatRepository.deleteByChatRoomId(chatRoomId);
    chatRoomRepository.delete(chatRoom);

    // TODO: 채팅방 삭제시 모임삭제 되도록
  }

  // 특정 사용자 강퇴 (호스트만 가능)
  @Transactional
  public ChatRoomDto withdrawal(User hostUser, Long chatRoomId, Long targetUserId) {
    User targetUser = validateUserExists(targetUserId);
    ChatRoom chatRoom = validateChatRoomExists(chatRoomId);

    // 호스트만 강제 퇴장 가능
    if (!chatRoom.getHost().getId().equals(hostUser.getId())) {
      throw new ChatException(ChatErrorCode.ONLY_HOST_CAN_OPERATE);
    }

    // 채팅방에서 퇴장 대상 사용자 제거
    chatRoom.getReader().remove(targetUser);
    chatRoomRepository.save(chatRoom);
    chatReadStatusRepository.deleteByUserIdAndChatRoomId(targetUserId, chatRoomId);

    // 강퇴하면 채팅방에 메시지를 발송
    messagingTemplate.convertAndSend("/sub/chat/room/" + chatRoomId,
        targetUser.getNickname() + "님이 강제퇴장되었습니다.");

    // TODO: 채팅방 강퇴시 모임강퇴 되도록

    return ChatRoomDto.of(chatRoom);
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

  private ChatReadStatus validateChatReadStatus(Long userId, Long chatRoomId) {
    return chatReadStatusRepository.findByUserIdAndChatRoomId(userId, chatRoomId)
        .orElseThrow(() -> new ChatException(ChatErrorCode.READ_STATUS_NOT_FOUND));
  }

  private void setUnreadMessagesCountToZero(Long chatRoomId, List<ChatRoomDto> rooms) {
    rooms.forEach(room -> {
      if (room.getRoomId().equals(chatRoomId)) {
        room.setUnreadMessagesCount(0);
      }
    });
  }

}