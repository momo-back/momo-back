package com.momo.meeting.repository;

import com.momo.meeting.entity.Meeting;
import com.momo.meeting.projection.MeetingToMeetingDtoProjection;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

  int countByUser_IdAndCreatedAtBetween(
      Long userId, LocalDateTime startOfDay, LocalDateTime endOfDay);

  @Query(value =
      "SELECT "
          + "dm.id as id, "
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
          + "  SELECT m.id, m.title, m.location_id, m.latitude, m.longitude, m.address, "
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

  /*@Query(value =
      "SELECT m.id as id, "
          + "m.title as title, "
          + "m.location_id as locationId, "
          + "m.latitude as latitude, "
          + "m.longitude as longitude, "
          + "m.address as address, "
          + "m.meeting_date_time as meetingDateTime, "
          + "m.max_count as maxCount, "
          + "m.approved_count as approvedCount, "
          + "m.thumbnail_url as thumbnailUrl, "
          + "GROUP_CONCAT(DISTINCT mc.category) as category, "
          + "ST_Distance_Sphere("
          + "    POINT(:userLongitude, :userLatitude), "
          + "    POINT(m.longitude, m.latitude)"
          + ") as distance "
          + "FROM meeting m "
          + "LEFT JOIN meeting_category mc ON m.id = mc.meeting_id "
          + "WHERE m.meeting_status = 'RECRUITING' "
          + "AND ST_Distance_Sphere("
          + "    POINT(:userLongitude, :userLatitude), "
          + "    POINT(m.longitude, m.latitude)"
          + ") <= :radius "
          + "AND ("
          + "    ST_Distance_Sphere("
          + "        POINT(:userLongitude, :userLatitude), "
          + "        POINT(m.longitude, m.latitude)"
          + "    ) > :lastDistance "
          + "    OR ("
          + "        ST_Distance_Sphere("
          + "            POINT(:userLongitude, :userLatitude), "
          + "            POINT(m.longitude, m.latitude)"
          + "        ) = :lastDistance "
          + "    AND m.id > :lastId)"
          + ") "
          + "GROUP BY m.id, m.title, m.location_id, m.latitude, m.longitude, "
          + "        m.address, m.meeting_date_time, m.max_count, m.approved_count, "
          + "        m.thumbnail_url "
          + "ORDER BY distance ASC, m.id ASC "
          + "LIMIT :pageSize",
      nativeQuery = true)
  List<MeetingToMeetingDtoProjection> findNearbyMeetingsWithCursor(
      @Param("userLatitude") double userLatitude,
      @Param("userLongitude") double userLongitude,
      @Param("radius") double radius,
      @Param("lastId") Long lastId,
      @Param("lastDistance") double lastDistance,
      @Param("pageSize") int pageSize
  );*/
}
