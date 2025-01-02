package com.momo.participation.repository;

import com.momo.participation.entity.Participation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {

  boolean existsByUser_IdAndMeeting_Id(Long userId, Long id);
}
