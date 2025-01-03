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
import java.util.ArrayList;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.UUID;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;
  private final JWTUtil jwtUtil;
  private final RefreshTokenRepository refreshTokenRepository;
  private final EmailService emailService;

  private final HashMap<String, String> passwordResetTokens = new HashMap<>();

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

  // 비밀번호 재설정 토큰 생성
  public String generateResetToken(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    String token = UUID.randomUUID().toString();
    passwordResetTokens.put(token, email);
    return token;
  }

  // 비밀번호 재설정 토큰 검증
  public boolean validateResetToken(String token) {
    return passwordResetTokens.containsKey(token);
  }

  // 비밀번호 재설정 링크 발송
  public void sendPasswordResetLink(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    String token = UUID.randomUUID().toString();
    passwordResetTokens.put(token, email);

    emailService.sendPasswordResetEmail(email, token);
  }

  // 비밀번호 재설정
  @Transactional
  public void resetPassword(String token, String newPassword) {
    String email = passwordResetTokens.get(token);
    if (email == null) {
      throw new CustomException(ErrorCode.INVALID_TOKEN);
    }

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);

    // 사용된 토큰 제거
    passwordResetTokens.remove(token);
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
    String email = kakaoProfile.getKakao_account().getEmail();

    // 이메일로 기존 사용자 검색
    User existingUser = userRepository.findByEmail(email).orElse(null);

    if (existingUser != null) {
      existingUser.setEnabled(true); // 사용자 활성화
      updateRefreshToken(existingUser, oauthToken.getRefresh_token()); // 여기서 호출
      return existingUser;
    }

    // 새 사용자 생성
    String randomPassword = UUID.randomUUID().toString();
    String encryptedPassword = passwordEncoder.encode(randomPassword);

    User kakaoUser = User.builder()
        .email(email)
        .nickname(email) // 닉네임 기본값으로 이메일 사용
        .password(encryptedPassword)
        .enabled(true)
        .oauthUser(true)
        .build();

    userRepository.save(kakaoUser); // 저장
    createRefreshToken(kakaoUser, oauthToken.getRefresh_token()); // 새 사용자에 대해 Refresh Token 생성
    return kakaoUser;
  }

  private void createRefreshToken(User user, String refreshTokenValue) {
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null");
    }

    if (user.getId() == null) {
      throw new IllegalStateException("User ID cannot be null");
    }

    if (user.getRefreshTokens() == null) {
      user.setRefreshTokens(new ArrayList<>());
    }

    RefreshToken refreshToken = new RefreshToken(user, refreshTokenValue);
    user.getRefreshTokens().add(refreshToken);
    refreshTokenRepository.save(refreshToken);
  }

  private void updateRefreshToken(User user, String newTokenValue) {
    if (user == null || user.getId() == null) {
      throw new IllegalArgumentException("User or User ID cannot be null");
    }

    refreshTokenRepository.deleteByUser(user);

    createRefreshToken(user, newTokenValue);
  }
}
