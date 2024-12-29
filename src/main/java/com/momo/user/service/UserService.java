package com.momo.user.service;

import com.momo.common.exception.CustomException;
import com.momo.common.exception.ErrorCode;
import com.momo.config.JWTUtil;
import com.momo.config.token.entity.RefreshToken;
import com.momo.config.token.repository.RefreshTokenRepository;
import com.momo.user.dto.CustomUserDetails;
import com.momo.auth.dto.KakaoProfile;
import com.momo.auth.dto.LoginDTO;
import com.momo.auth.dto.OAuthToken;
import com.momo.user.entity.User;
import com.momo.user.repository.UserRepository;
import java.util.UUID;
import javax.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;
  private final JWTUtil jwtUtil;
  private final RefreshTokenRepository refreshTokenRepository;

  public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder,
      JWTUtil jwtUtil, RefreshTokenRepository refreshTokenRepository) {
    this.userRepository = userRepository;
    this.jwtUtil = jwtUtil;
    this.refreshTokenRepository = refreshTokenRepository;
    this.passwordEncoder = passwordEncoder;
  }

  // 로그인 처리
  public String loginUser(LoginDTO loginDto) {
    User user = userRepository.findByEmail(loginDto.getEmail())
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
      throw new RuntimeException("Invalid credentials");
    }

    // Access Token 발급
    return jwtUtil.createJwt("access", user, 600000L); // 10분 만료
  }

  // 회원 탈퇴
  public void deleteUser() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();

    // User 조회
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    // RefreshToken 삭제
    if (refreshTokenRepository.existsByToken(email)) {
      refreshTokenRepository.deleteByEmail(email);
    }

    // User 삭제
    userRepository.delete(user);
  }

  // 현재 로그인한 사용자 정보 가져오기
  private CustomUserDetails getLoggedInUserDetails() {
    return (CustomUserDetails) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal();
  }

  @Transactional
  public User processKakaoUser(KakaoProfile kakaoProfile, OAuthToken oauthToken) {
    // 1. 이메일 및 닉네임 처리
    String email = kakaoProfile.getKakao_account().getEmail();
    String uniqueNickname = email;

    // 2. 기존 사용자 확인
    User existingUser = userRepository.findByEmail(email).orElse(null);

    if (existingUser != null) {
      existingUser.setEnabled(true); // 기존 사용자는 활성화
      updateRefreshToken(existingUser, oauthToken.getRefresh_token()); // RefreshToken 갱신
      return existingUser;
    }

    // 3. 새로운 사용자 생성
    String randomPassword = UUID.randomUUID().toString();
    String encryptedPassword = passwordEncoder.encode(randomPassword);

    User kakaoUser = User.builder()
        .email(email)
        .nickname(uniqueNickname)
        .phone("") // 카카오 API에서는 전화번호를 제공하지 않음
        .password(encryptedPassword)
        .verificationToken(null)
        .enabled(true)
        .build();

    // 4. RefreshToken 생성 및 추가
    createRefreshToken(kakaoUser, oauthToken.getRefresh_token());

    // 5. 사용자 저장
    return userRepository.save(kakaoUser);
  }

  private void createRefreshToken(User user, String refreshTokenValue) {
    RefreshToken refreshToken = new RefreshToken(user, refreshTokenValue);
    user.getRefreshTokens().add(refreshToken);
  }

  private void updateRefreshToken(User user, String newTokenValue) {
    // 기존 RefreshToken 삭제
    refreshTokenRepository.deleteByUser(user);

    // 새로운 RefreshToken 생성 및 추가
    createRefreshToken(user, newTokenValue);
  }
}
