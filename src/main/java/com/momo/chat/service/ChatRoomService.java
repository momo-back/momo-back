package com.momo.chat.service;

import com.momo.chat.dto.ChatRoomResponseDto;
import com.momo.chat.entity.ChatRoom;
import com.momo.chat.repository.ChatRepository;
import com.momo.chat.repository.ChatRoomRepository;
import com.momo.meeting.entity.Meeting;
import com.momo.meeting.repository.MeetingRepository;
import com.momo.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

  private final ChatRepository chatRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final MeetingRepository meetingRepository;

  // 채팅방 생성
  public ChatRoomResponseDto createChatRoom(Long userId, long meetingId) {
    Meeting meeting = meetingRepository.findById(meetingId)
        .orElseThrow(() -> new RuntimeException("해당 모임이 없습니다."));
    User user = meeting.getUser();
    ChatRoom chatRoom = ChatRoom.builder()
        .host(user)
        .meeting(meeting)
        .build();

    chatRoomRepository.save(chatRoom);

    return ChatRoomResponseDto.of(chatRoom);

  }






//  public void createChatRoom(User user, Long meetingId) {
//    Meeting meeting = meetingRepository.findById(meetingId).orElseThrow();
//    ChatRoom chatRoom = ChatRoom.builder()
//                                .user(user)
//                                .meeting(meeting)
//                                .build();
//    chatRoomRepository.save(chatRoom);
//  }

}
