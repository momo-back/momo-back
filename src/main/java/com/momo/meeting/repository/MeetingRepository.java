package com.momo.meeting.repository;

import com.momo.meeting.constant.MeetingStatus;
import com.momo.meeting.entity.Meeting;
import com.momo.meeting.projection.CreatedMeetingProjection;
import com.momo.meeting.projection.ExpiredMeetingProjection;
import com.momo.meeting.projection.MeetingToMeetingDtoProjection;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

  int countByUser_IdAndCreatedAtBetween(
      Long userId, LocalDateTime startOfDay, LocalDateTime endOfDay);

  // 모집글 목록을 meeting_date_time 을 기준으로 오름차순 정렬하여 반환 (커서 기반)
  // meeting_date_time이 초단위까지 저장되면 현재 방식에서는 중복데이터가 발생할 수 있음.
  // lastMeetingDateTime이 분단위까지만 표현하고 있기 때문.
  @Query(value =
      "SELECT "
          + "m.id as id, "
          + "m.user_id as authorId, "
          + "m.title as title, "
          + "m.location_id as locationId, "
          + "m.latitude as latitude, "
          + "m.longitude as longitude, "
          + "m.address as address, "
          + "m.meeting_date_time as meetingDateTime, "
          + "m.max_count as maxCount, "
          + "m.approved_count as approvedCount, "
          + "m.content as content, "
          + "m.thumbnail_url as thumbnailUrl, "
          + "GROUP_CONCAT(mc.category) as category "
          + "FROM meeting m "
          + "INNER JOIN meeting_category mc ON m.id = mc.meeting_id "
          + "WHERE m.meeting_status = 'RECRUITING' "
          + "AND ("
          + "  m.meeting_date_time > :lastDateTime "
          + "  OR (m.meeting_date_time = :lastDateTime AND m.id > :lastId) "
          + ") "
          + "GROUP BY "
          + "  m.id, m.user_id, m.title, "
          + "  m.location_id, m.latitude, m.longitude, "
          + "  m.address, m.meeting_date_time, m.max_count, "
          + "  m.approved_count, m.content, m.thumbnail_url "
          + "ORDER BY m.meeting_date_time ASC, m.id ASC  "
          + "LIMIT :pageSize",
      nativeQuery = true)
  List<MeetingToMeetingDtoProjection> findOrderByMeetingDateWithCursor(
      @Param("lastId") Long lastId,
      @Param("lastDateTime") LocalDateTime lastDateTime,
      @Param("pageSize") int pageSize
  );

  // 모집글 목록을 사용자의 거리와 가까운 순서대로 반환 (커서 기반)
  @Query(value =
      "SELECT "
          + "dm.id as id, "
          + "dm.user_id as authorId, "
          + "dm.title as title, "
          + "dm.location_id as locationId, "
          + "dm.latitude as latitude, "
          + "dm.longitude as longitude, "
          + "dm.address as address, "
          + "dm.meeting_date_time as meetingDateTime, "
          + "dm.max_count as maxCount, "
          + "dm.approved_count as approvedCount, "
          + "dm.content as content, "
          + "dm.thumbnail_url as thumbnailUrl, "
          + "("
          + "  SELECT GROUP_CONCAT(mc.category) "
          + "  FROM meeting_category mc "
          + "  WHERE mc.meeting_id = dm.id "
          + ") as category, "
          + "dm.distance as distance "
          + "FROM ("
          + "  SELECT m.id, m.user_id, m.title, m.location_id, m.latitude, m.longitude, m.address, "
          + "  m.meeting_date_time, m.max_count, m.approved_count, m.content, m.thumbnail_url, "
          + "    ST_Distance_Sphere( "
          + "        POINT(:userLongitude, :userLatitude), "
          + "        POINT(m.longitude, m.latitude) "
          + "    ) as distance "
          + "  FROM meeting m "
          + "  WHERE m.meeting_status = 'RECRUITING' "
          + ") dm "
          + "WHERE dm.distance <= :radius "
          + "AND ("
          + "  dm.distance > :lastDistance "
          + "  OR (dm.distance = :lastDistance AND dm.id > :lastId) "
          + ")"
          + "ORDER BY dm.distance ASC, dm.id ASC "
          + "LIMIT :pageSize",
      nativeQuery = true)
  List<MeetingToMeetingDtoProjection> findNearbyMeetingsWithCursor(
      @Param("userLatitude") double userLatitude,
      @Param("userLongitude") double userLongitude,
      @Param("radius") double radius,
      @Param("lastId") Long lastId,
      @Param("lastDistance") double lastDistance,
      @Param("pageSize") int pageSize
  );

  // 개최한 모임 목록을 생성된 순서대로 반환 (커서 기반)
  @Query(value = "SELECT "
      + "m.user_id as userId, "
      + "m.id as meetingId, "
      + "m.meeting_status as meetingStatus, "
      + "m.title as title, "
      + "m.location_id as locationId, "
      + "m.latitude as latitude, "
      + "m.longitude as longitude, "
      + "m.address as address, "
      + "m.meeting_date_time as meetingDateTime, "
      + "m.max_count as maxCount, "
      + "m.approved_count as approvedCount, "
      + "GROUP_CONCAT(mc.category) as category, "
      + "m.content as content, "
      + "m.thumbnail_url as thumbnailUrl "
      + "FROM meeting as m "
      + "INNER JOIN meeting_category mc ON mc.meeting_id = m.id "
      + "WHERE m.user_id = :userId "
      + "AND m.id > :lastId "
      + "GROUP BY m.id, m.user_id, m.meeting_status, m.title, "
      + "m.location_id, m.latitude, m.longitude, m.address, "
      + "m.meeting_date_time, m.max_count, m.approved_count, "
      + "m.content, m.thumbnail_url "
      + "ORDER BY m.meeting_date_time ASC, m.id ASC  "
      + "LIMIT :pageSize",
      nativeQuery = true)
  List<CreatedMeetingProjection> findAllByUser_IdOrderByCreatedAtAsc(
      @Param("userId") Long userId,
      @Param("lastId") Long lastId,
      @Param("pageSize") int pageSize
  );

  @Query("SELECT DISTINCT "
      + "m.id as meetingId, "
      + "m.title as title, "
      + "m.user as author " +
      "FROM Meeting m " +
      "WHERE m.meetingStatus = :meetingStatus " +
      "AND m.meetingDateTime < :now")
  List<ExpiredMeetingProjection> findExpiredMeetings(
      @Param("meetingStatus") MeetingStatus meetingStatus,
      @Param("now") LocalDateTime now
  );

  @Modifying
  @Query("DELETE FROM Meeting m WHERE m.id IN :meetingIds")
  int deleteAllByMeetingIds(@Param("meetingIds") List<Long> meetingIds);
}
