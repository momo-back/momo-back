package com.momo.profile.repository;

import com.momo.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

  boolean existsByUser_Id(Long userId);
}
