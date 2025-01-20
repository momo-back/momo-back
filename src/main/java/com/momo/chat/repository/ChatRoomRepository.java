package com.momo.chat.repository;

import com.momo.chat.entity.ChatRoom;
import com.momo.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

  Optional<ChatRoom> findById(Long roomId);

  List<ChatRoom> findAllByReaderContains(User user);

  Optional<ChatRoom> findByMeeting_Id(Long id);

  @Modifying
  @Query("DELETE FROM ChatRoom cr WHERE cr.meeting.id IN :meetingIds")
  int deleteAllByMeetingIds(@Param("meetingIds") List<Long> meetingIds);

  // 유저가 생성한 채팅방 삭제
  @Modifying
  @Query("DELETE FROM ChatRoom cr WHERE cr.host.id = :userId")
  void deleteByHostId(@Param("userId") Long userId);

  // 유저를 채팅방에서 제거
  default void removeUserFromChatRooms(User user) {
    List<ChatRoom> chatRooms = findAllByReaderContains(user);
    for (ChatRoom chatRoom : chatRooms) {
      chatRoom.getReader().remove(user);
    }
    saveAll(chatRooms); // 변경 사항 저장
  }
}
