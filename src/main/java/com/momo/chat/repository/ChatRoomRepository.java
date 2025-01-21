package com.momo.chat.repository;

import com.momo.chat.entity.ChatRoom;
import com.momo.meeting.entity.Meeting;
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

  List<ChatRoom> findByMeeting(Meeting meeting);
}
