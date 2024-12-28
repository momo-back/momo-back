package com.momo.profile.persist.reposiroty;

import com.momo.profile.persist.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

  boolean existsByUser_Id(Long userId);
}
