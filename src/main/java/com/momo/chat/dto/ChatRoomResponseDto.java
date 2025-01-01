package com.momo.chat.dto;

import com.momo.chat.entity.ChatRoom;
import com.momo.user.entity.User;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRoomResponseDto {

  private Long roomId;
  private Long meetingId;
  private Long hostId;
  private List<Long> readerId;

  public ChatRoomResponseDto(Long roomId, Long meetingId, Long hostId, List<Long> readerId) {
    this.roomId = roomId;
    this.meetingId = meetingId;
    this.hostId = hostId;
    this.readerId = readerId;
  }

  public static ChatRoomResponseDto of(ChatRoom chatRoom) {
    List<Long> readerId = new ArrayList<>();

    // chatRoom.getReader()가 List<User> 타입인 경우에 대비하여 처리
    if (chatRoom.getReader() != null && !chatRoom.getReader().isEmpty()) {
      readerId = chatRoom.getReader().stream()
          .map(User::getId)
          .collect(Collectors.toList());
    }

    return new ChatRoomResponseDto(chatRoom.getId(), chatRoom.getMeeting().getId(), chatRoom.getHost().getId(), readerId);

  }

}
