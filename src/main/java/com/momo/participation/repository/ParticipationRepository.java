package com.momo.participation.repository;

import com.momo.meeting.projection.MeetingParticipantProjection;
import com.momo.participation.constant.ParticipationStatus;
import com.momo.participation.entity.Participation;
import com.momo.participation.projection.AppliedMeetingProjection;
import com.momo.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {

  boolean existsByUser_IdAndMeeting_Id(Long userId, Long id);

  // 신청한 모임 목록 조회
  @Query(value =
      "SELECT "
          + "p.id as id, "
          + "m.id as meetingId, "
          + "m.user_id as authorId, "
          + "p.participation_status as participationStatus, "
          + "m.title as title, "
          + "m.location_id as locationId, "
          + "m.latitude as latitude, "
          + "m.longitude as longitude, "
          + "m.address as address, "
          + "m.meeting_date_time as meetingDateTime, "
          + "m.max_count as maxCount, "
          + "m.approved_count as approvedCount, "
          + "categories.categories as category, "
          + "m.content as content, "
          + "m.thumbnail as thumbnail "
          + "FROM participation p "
          + "INNER JOIN meeting m ON p.meeting_id = m.id "
          + "INNER JOIN ("
          + "    SELECT meeting_id, GROUP_CONCAT(category) as categories "
          + "    FROM meeting_category "
          + "    GROUP BY meeting_id"
          + ") categories ON m.id = categories.meeting_id "
          + "WHERE p.user_id = :userId "
          + "AND p.id > :lastId "
          + "ORDER BY p.id ASC "
          + "LIMIT :pageSize",
      nativeQuery = true)
  List<AppliedMeetingProjection> findAppliedMeetingsWithLastId(
      @Param("userId") Long userId,
      @Param("lastId") Long lastId,
      @Param("pageSize") int pageSize
  );

  // 해당 모임에 참여 신청한 회원 목록을 조회
  @Query(value = "SELECT "
      + "u.user_id as userId, "
      + "mp.id as participationId, "
      + "u.nickname as nickname, "
      + "p.profile_image_url as profileImage, "
      + "mp.participation_status as participationStatus "
      + "FROM participation mp "
      + "JOIN users u ON mp.user_id = u.user_id "
      + "JOIN profile p ON u.user_id = p.user_id "
      + "WHERE mp.meeting_id = :meetingId "
      + "ORDER BY mp.created_at ASC",
      nativeQuery = true)
  List<MeetingParticipantProjection> findMeetingParticipantsByMeeting_Id(
      @Param("meetingId") Long meetingId
  );


  void deleteByUser_Id(Long id);

  Optional<Participation> findByUser_Id(Long id);

  @Query("SELECT p.user FROM Participation p " +
      "WHERE p.meeting.id IN :meetingIds " +
      "  AND p.participationStatus = :participationStatus")
  List<User> findParticipantsByMeetingIds(
      @Param("meetingIds") List<Long> meetingIds,
      @Param("participationStatus") ParticipationStatus participationStatus
  );

  @Modifying
  @Query("DELETE FROM Participation p WHERE p.meeting.id IN :meetingIds")
  int deleteAllByMeetingIds(@Param("meetingIds") List<Long> meetingIds);

  List<Participation> findByUserId(Long userId);

  // 특정 미팅에 대한 모든 참여자 삭제
  @Modifying
  @Query("DELETE FROM Participation p WHERE p.meeting.id = :meetingId")
  void deleteByMeetingId(Long meetingId);


  @Modifying
  @Query("UPDATE Participation p SET p.participationStatus = :newStatus "
      + "WHERE p.meeting.id = :meetingId AND p.participationStatus  = :currentStatus")
  void findAllByMeeting_IdAndParticipationStatus(
      @Param("meetingId") Long meetingID,
      @Param("currentStatus") ParticipationStatus currentStatus,
      @Param("newStatus") ParticipationStatus newStatus);

  List<Participation> findAllByMeeting_Id(Long meetingId);
}
