package com.momo.meeting.repository;

import com.momo.meeting.entity.Meeting;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

  int countByUser_IdAndCreatedAtBetween(Long userId, LocalDateTime startOfDay, LocalDateTime endOfDay);
}
