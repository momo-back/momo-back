package com.momo.user.service;

import com.momo.auth.service.KakaoAuthService;
import com.momo.common.exception.CustomException;
import com.momo.common.exception.ErrorCode;
import com.momo.config.JWTUtil;
import com.momo.config.token.entity.RefreshToken;
import com.momo.config.token.repository.RefreshTokenRepository;
import com.momo.profile.entity.Profile;
import com.momo.profile.repository.ProfileRepository;
import com.momo.user.dto.CustomUserDetails;
import com.momo.auth.dto.KakaoProfile;
import com.momo.auth.dto.LoginDTO;
import com.momo.auth.dto.OAuthToken;
import com.momo.user.dto.OtherUserInfoResponse;
import com.momo.user.dto.UserInfoResponse;
import com.momo.user.dto.UserUpdateRequest;
import com.momo.user.entity.User;
import com.momo.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;
  private final JWTUtil jwtUtil;
  private final RefreshTokenRepository refreshTokenRepository;
  private final EmailService emailService;
  private final ProfileRepository profileRepository;
  private final KakaoAuthService kakaoAuthService;

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

  @Transactional
  public void deleteUserWithKakaoUnlink(HttpServletRequest request, HttpServletResponse response) {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    // 카카오 로그인 회원인지 확인
    if (user.isOauthUser()) {
      String accessToken = extractAccessTokenFromAuthorizationHeader(request);
      if (accessToken != null) {
        // 카카오 탈퇴 API 호출
        kakaoAuthService.unlinkKakaoAccount(accessToken);
      }
    }

    // RefreshToken 삭제
    refreshTokenRepository.deleteByUser(user);

    // Profile 삭제
    profileRepository.findByUser(user).ifPresent(profileRepository::delete);

    // User 삭제
    userRepository.delete(user);

    // Refresh 쿠키 삭제
    clearRefreshCookie(response);

    log.debug("User and related data deleted successfully for email: {}", email);
  }
  private String extractAccessTokenFromAuthorizationHeader(HttpServletRequest request) {
    String authorizationHeader = request.getHeader("Authorization");
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      return authorizationHeader.substring(7);
    }
    return null;
  }

  private void clearRefreshCookie(HttpServletResponse response) {
    Cookie cookie = new Cookie("refresh", null);
    cookie.setMaxAge(0);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    response.addCookie(cookie);
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

  public UserInfoResponse getUserInfo() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found"));

    Profile profile = profileRepository.findByUser(user)
        .orElseThrow(() -> new RuntimeException("Profile not found"));

    return UserInfoResponse.builder()
        .nickname(user.getNickname())
        .phone(user.getPhone())
        .email(user.getEmail())
        .gender(profile.getGender())
        .birth(profile.getBirth())
        .profileImageUrl(profile.getProfileImageUrl())
        .introduction(profile.getIntroduction())
        .mbti(profile.getMbti())
        .build();
  }

  // 카카오 로그인 회원정보 조회
  public UserInfoResponse getUserInfoByEmail(String email) {
    // User 엔티티 조회
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    // Profile 엔티티 조회
    Profile profile = profileRepository.findByUser(user)
        .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));

    // UserInfoResponse 생성 및 반환
    return UserInfoResponse.builder()
        .nickname(user.getNickname())
        .phone(user.getPhone())
        .email(user.getEmail())
        .gender(profile.getGender())
        .birth(profile.getBirth())
        .profileImageUrl(profile.getProfileImageUrl())
        .introduction(profile.getIntroduction())
        .mbti(profile.getMbti())
        .oauthProvider(user.isOauthUser() ? "KAKAO" : "LOCAL") // 회원 타입 구분
        .build();
  }

  @Transactional
  public void updateUser(UserUpdateRequest updateRequest) {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();

    // User 엔티티 조회
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    // Profile 엔티티 조회 및 업데이트
    Profile profile = profileRepository.findByUser(user)
        .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));

    // User 엔티티 업데이트
    if (updateRequest.getNickname() != null) {
      user.setNickname(updateRequest.getNickname());
    }
    if (updateRequest.getPhone() != null) {
      user.setPhone(updateRequest.getPhone());
    }
    userRepository.save(user);

    // Profile 엔티티 업데이트
    if (updateRequest.getProfileImageUrl() != null) {
      profile.setProfileImageUrl(updateRequest.getProfileImageUrl());
    }
    if (updateRequest.getIntroduction() != null) {
      profile.setIntroduction(updateRequest.getIntroduction());
    }
    if (updateRequest.getMbti() != null) {
      profile.setMbti(updateRequest.getMbti());
    }
    profileRepository.save(profile);

    log.debug("User and Profile updated successfully for email: {}", email);
  }

  @Transactional
  public OtherUserInfoResponse getOtherUserProfile(Long userId) {
    // User 엔티티 조회
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    // Profile 엔티티 조회
    Profile profile = profileRepository.findByUser(user)
        .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));

    // 필요한 정보만 반환
    return OtherUserInfoResponse.builder()
        .nickname(user.getNickname())
        .gender(profile.getGender())
        .birth(profile.getBirth())
        .mbti(profile.getMbti())
        .introduction(profile.getIntroduction())
        .build();
  }

}
