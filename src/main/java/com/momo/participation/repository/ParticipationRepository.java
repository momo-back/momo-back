package com.momo.participation.repository;

import com.momo.participation.entity.Participation;
import com.momo.participation.projection.AppliedMeetingProjection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {

  boolean existsByUser_IdAndMeeting_Id(Long userId, Long id);

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
          + "m.thumbnail_url as thumbnailUrl "
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
}
