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
import com.momo.notification.constant.NotificationType;
import com.momo.notification.service.NotificationService;
import com.momo.user.entity.User;
import com.momo.user.repository.UserRepository;
import java.util.List;
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
  private final NotificationService notificationService;


  @Transactional
  public void sendMessage(ChatRequestDto dto) {
    User user = userRepository.findById(dto.getUserId())
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

    // 채팅방 참여자들에게 알림 전송
    sendChatRoomNotification(chatRoom, user, dto.getMessage());
  }

  private void sendChatRoomNotification(ChatRoom chatRoom, User sender, String message) {
    // 채팅방에 참여한 사용자 목록 가져오기
    List<User> readers = chatRoom.getReader();

    // "닉네임: 채팅내용" 형식으로 알림 내용 구성
    String notificationContent = sender.getNickname() + ": " + message;

    // 참여자들에게 알림 전송
    for (User reader : readers) {
      if (!reader.getId().equals(sender.getId())) {  // 보낸 사람에게는 알림을 보내지 않음
        notificationService.sendNotification(reader, notificationContent, NotificationType.NEW_CHAT_MESSAGE);
      }
    }
  }

}