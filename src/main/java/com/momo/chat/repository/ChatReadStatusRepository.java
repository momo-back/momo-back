package com.momo.chat.repository;

import com.momo.chat.entity.ChatReadStatus;
import com.momo.chat.entity.ChatRoom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatReadStatusRepository extends JpaRepository<ChatReadStatus, Long> {

  Optional<ChatReadStatus> findByUserIdAndChatRoomId(Long userId, Long chatRoomId);

  void deleteByUserIdAndChatRoomId(Long userId, Long chatRoomId);

  void deleteByChatRoomId(Long chatRoomId);

  void deleteByChatRoom(ChatRoom chatRoom);

  void deleteByChatRoom_IdAndUser_Id(Long chatRoomId, Long userId);

  @Modifying
  @Query("DELETE FROM ChatReadStatus crs WHERE crs.chatRoom.id IN :chatRoomIds")
  void deleteAllByChatRoomIds(@Param("chatRoomIds") List<Long> chatRoomIds);
}
