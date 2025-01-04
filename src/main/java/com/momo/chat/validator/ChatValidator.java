package com.momo.chat.validator;

import com.momo.chat.entity.ChatRoom;
import com.momo.chat.exception.ChatErrorCode;
import com.momo.chat.exception.ChatException;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatValidator {

  private final ChatRoomRepository chatRoomRepository;
  private final UserRepository userRepository;
  private final MeetingRepository meetingRepository;
  private final ProfileRepository profileRepository;

  public ChatRoom validateUserParticipation(Long userId, Long chatRoomId) {
    ChatRoom chatRoom = validateChatRoomExists(chatRoomId);
    User user = validateUserExists(userId);

    if (!chatRoom.getReader().contains(user)) {
      throw new ChatException(ChatErrorCode.NOT_A_PARTICIPANT);
    }
    return chatRoom;
  }

  public void validateHostOperation(Long userId, Long chatRoomId) {
    ChatRoom chatRoom = validateChatRoomExists(chatRoomId);
    User user = validateUserExists(userId);

    if (!chatRoom.getHost().getId().equals(user.getId())) {
      throw new ChatException(ChatErrorCode.ONLY_HOST_CAN_OPERATE);
    }
  }

  public ChatRoom validateChatRoomExists(Long chatRoomId) {
    return chatRoomRepository.findById(chatRoomId)
        .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
  }

  public User validateUserExists(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
  }

  public Profile validateProfileExists(Long userId) {
    return profileRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_EXISTS_PROFILE));
  }

  public Meeting validateMeetingExists(Long meetingId) {
    return meetingRepository.findById(meetingId)
        .orElseThrow(() -> new MeetingException(MeetingErrorCode.MEETING_NOT_FOUND));
  }
}