package com.momo.chat.service;

import com.momo.chat.dto.ChatRequestDto;
import com.momo.chat.dto.ChatResponseDto;
import com.momo.chat.entity.Chat;
import com.momo.chat.entity.ChatRoom;
import com.momo.chat.exception.ChatErrorCode;
import com.momo.chat.exception.ChatException;
import com.momo.chat.repository.ChatRepository;
import com.momo.chat.repository.ChatRoomRepository;
import com.momo.common.exception.CustomException;
import com.momo.common.exception.ErrorCode;
import com.momo.user.entity.User;
import com.momo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

  private final SimpMessageSendingOperations messagingTemplate;
  private final ChatRepository chatRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final UserRepository userRepository;

  @Transactional
  public void sendMessage(Long userId, ChatRequestDto dto) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    ChatRoom chatRoom = chatRoomRepository.findById(dto.getRoomId())
        .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

    Chat chat = Chat.builder()
        .sender(user)
        .chatRoom(chatRoom)
        .message(dto.getMessage())
        .build();

    chatRepository.save(chat);

    ChatResponseDto sendMessage = new ChatResponseDto();
    sendMessage.setRoomId(dto.getRoomId());
    sendMessage.setMessage(dto.getMessage());
    sendMessage.setSender(user.getNickname());

    messagingTemplate.convertAndSend("/sub/chat/room/" + dto.getRoomId(), sendMessage);
  }

  @Transactional
  public void enterRoomMessage(Long userId, ChatRequestDto dto) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    messagingTemplate.convertAndSend("/sub/chat/room/" + dto.getRoomId(),
        user.getNickname() + "님이 입장했습니다.");
  }

  @Transactional
  public void leaveRoomMessage(Long userId, ChatRequestDto dto) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    messagingTemplate.convertAndSend("/sub/chat/room/" + dto.getRoomId(),
        user.getNickname() + "님이 퇴장했습니다.");
  }


}