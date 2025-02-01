package com.momo.chat.repository;

import com.momo.chat.entity.Chat;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

  List<Chat> findAllByChatRoomId(Long chatRoomId);

  void deleteByChatRoomId(Long chatRoomId);

  @Modifying
  @Query("DELETE FROM Chat c WHERE c.chatRoom.id IN :chatRoomIds")
  void deleteAllByChatRoomIds(@Param("chatRoomIds") List<Long> chatRoomIds);
}
