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
public class ChatRoomDto {

  private Long roomId;
  private Long meetingId;
  private String meetingThumbnailUrl;
  private String meetingTitle;
  private String lastMessage;
  private Long hostId;
  private List<Long> readerId;
  private Integer unreadMessagesCount;

  public ChatRoomDto(Long roomId, Long meetingId, String meetingThumbnailUrl,
      String meetingTitle, String lastMessage, Long hostId, List<Long> readerId) {
    this.roomId = roomId;
    this.meetingId = meetingId;
    this.meetingThumbnailUrl = meetingThumbnailUrl;
    this.meetingTitle = meetingTitle;
    this.lastMessage = lastMessage;
    this.hostId = hostId;
    this.readerId = readerId;
  }

  public ChatRoomDto(Long roomId, Long meetingId, String meetingThumbnailUrl, String meetingTitle,
      String lastMessage, Long hostId, List<Long> readerId, Integer unreadMessagesCount) {
    this.roomId = roomId;
    this.meetingId = meetingId;
    this.meetingThumbnailUrl = meetingThumbnailUrl;
    this.meetingTitle = meetingTitle;
    this.lastMessage = lastMessage;
    this.hostId = hostId;
    this.readerId = readerId;
    this.unreadMessagesCount = unreadMessagesCount == null ? 0 : unreadMessagesCount;
  }

  public static ChatRoomDto of(ChatRoom chatRoom) {
    List<Long> readerId = new ArrayList<>();

    // chatRoom.getReader()가 List<User> 타입인 경우에 대비하여 처리
    if (chatRoom.getReader() != null && !chatRoom.getReader().isEmpty()) {
      readerId = chatRoom.getReader().stream()
          .map(User::getId)
          .collect(Collectors.toList());
    }

    // chatRoom.getChatMessages()가 비어 있는지 확인하고, 비어있지 않다면 마지막 메시지를 가져옴
    String lastMessage = "";
    if (!chatRoom.getChatMessages().isEmpty()) {
      lastMessage = chatRoom.getChatMessages().get(chatRoom.getChatMessages().size() - 1).getMessage();
    }

    return new ChatRoomDto(
        chatRoom.getId(),
        chatRoom.getMeeting().getId(),
        chatRoom.getMeeting().getThumbnailUrl(),
        chatRoom.getMeeting().getTitle(),
        lastMessage,
        chatRoom.getHost().getId(),
        readerId);
  }


}
