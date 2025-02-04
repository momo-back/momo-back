package com.momo.user.repository;

import com.momo.user.dto.OtherUserInfoProjection;
import com.momo.user.dto.UserInfoProjection;
import com.momo.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {

  boolean existsByEmail(String email);

  boolean existsByNickname(String nickname);

  boolean existsByPhone(String phone);
  Optional<User> findByVerificationToken(String token);
  Optional<User> findByEmail(String email);

  Optional<User> findById(Long userId);

  // 본인 정보 조회 - User와 Profile을 JOIN하여 필요한 필드만 조회
  @Query("SELECT u.nickname as nickname, u.phone as phone, u.email as email, " +
      "p.gender as gender, p.birth as birth, p.profileImageUrl as profileImageUrl, " +
      "p.introduction as introduction, p.mbti as mbti, u.oauthUser as oauthUser " +
      "FROM User u JOIN Profile p ON u.id = p.user.id " +
      "WHERE u.email = :email")
  Optional<UserInfoProjection> findUserInfoByEmail(String email);

  // 다른 사용자 프로필 조회 - User와 Profile을 JOIN하여 필요한 필드만 조회
  @Query("SELECT u.nickname as nickname, p.gender as gender, p.birth as birth, " +
      "p.mbti as mbti, p.introduction as introduction, p.profileImageUrl as profileImageUrl " +
      "FROM User u JOIN Profile p ON u.id = p.user.id " +
      "WHERE u.id = :userId")
  Optional<OtherUserInfoProjection> findOtherUserProfileById(Long userId);

}

