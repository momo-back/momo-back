package com.momo.chat.repository;

import com.momo.chat.entity.ChatRoom;
import com.momo.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

  Optional<ChatRoom> findById(Long roomId);

  List<ChatRoom> findAllByReaderContains(User user);

}
