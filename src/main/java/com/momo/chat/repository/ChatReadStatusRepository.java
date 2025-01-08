package com.momo.chat.repository;

import com.momo.chat.entity.ChatReadStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatReadStatusRepository extends JpaRepository<ChatReadStatus, Long> {

  Optional<ChatReadStatus> findByUserIdAndChatRoomId(Long userId, Long chatRoomId);

  void deleteByUserIdAndChatRoomId(Long userId, Long chatRoomId);

  void deleteByChatRoomId(Long chatRoomId);

}
