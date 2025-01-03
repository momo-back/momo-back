package com.momo.chat.service;

import com.momo.chat.dto.ChatRequestDto;
import com.momo.chat.dto.ChatResponseDto;
import com.momo.chat.entity.Chat;
import com.momo.chat.entity.ChatRoom;
import com.momo.chat.repository.ChatRepository;
import com.momo.chat.validator.ChatValidator;
import com.momo.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

  private final SimpMessageSendingOperations messagingTemplate;
  private final ChatRepository chatRepository;
  private final ChatValidator chatValidator;

  @Transactional
  public void sendMessage(Long userId, ChatRequestDto dto) {
    ChatRoom chatRoom = chatValidator.validateChatRoomExists(dto.getRoomId());
    User user = chatValidator.validateUserExists(userId);

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
  public void enterRoom(Long userId, ChatRequestDto dto) {
    User user = chatValidator.validateUserExists(userId);
    messagingTemplate.convertAndSend("/sub/chat/room/" + dto.getRoomId(),
        user.getNickname() + "님이 입장했습니다.");
  }

  @Transactional
  public void leaveRoom(Long userId, ChatRequestDto dto) {
    User user = chatValidator.validateUserExists(userId);
    messagingTemplate.convertAndSend("/sub/chat/room/" + dto.getRoomId(),
        user.getNickname() + "님이 퇴장했습니다.");
  }


}
