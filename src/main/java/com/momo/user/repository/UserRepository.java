package com.momo.user.repository;

import com.momo.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  boolean existsByEmail(String email);

  boolean existsByNickname(String nickname);

  boolean existsByPhone(String phone);
  Optional<User> findByVerificationToken(String token);
  Optional<User> findByEmail(String email);

}

