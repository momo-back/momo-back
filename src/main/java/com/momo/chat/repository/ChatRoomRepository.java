package com.momo.chat.repository;

import com.momo.chat.entity.ChatRoom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

  Optional<ChatRoom> findById(Long roomId);

  Optional<ChatRoom> findByMeetingId(Long meetingId);

}
