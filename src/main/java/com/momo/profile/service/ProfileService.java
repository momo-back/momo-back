package com.momo.profile.service;

import com.momo.common.exception.CustomException;
import com.momo.common.exception.ErrorCode;
import com.momo.config.JWTUtil;
import com.momo.config.token.entity.RefreshToken;
import com.momo.config.token.repository.RefreshTokenRepository;
import com.momo.profile.constant.Gender;
import com.momo.profile.dto.ProfileCreateRequest;
import com.momo.profile.dto.ProfileCreateResponse;
import com.momo.profile.entity.Profile;
import com.momo.profile.repository.ProfileRepository;
import com.momo.profile.validation.ProfileRequiredValueValidator;
import com.momo.profile.validation.ProfileValidator;
import com.momo.user.entity.User;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

  private final ProfileValidator profileValidator;
  private final ProfileRepository profileRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final ProfileImageService profileImageService;
  private final JWTUtil jwtUtil;

  @Transactional
  public ProfileCreateResponse createProfile(
      HttpServletResponse response,
      User user,
      ProfileCreateRequest profileCreateRequest,
      MultipartFile profileImage
  ) {
    validateForProfileCreation(
        user.getId(), profileCreateRequest.getGender(), profileCreateRequest.getBirth()
    ); // 검증
    Profile savedProfile = saveProfile(user, profileCreateRequest, profileImage); // 프로필 저장
    user.setProfile(savedProfile);
    String newAccessToken = updateTokens(response, user); // 프로필 생성 시 새로운 토큰 발급

    return ProfileCreateResponse.from(savedProfile, newAccessToken);
  }

  private void validateForProfileCreation(Long userId, Gender gender, LocalDate birth) {
    profileValidator.validateHasProfile(userId);
    ProfileRequiredValueValidator.validateProfileRequiredValue(gender, birth);
  }

  private Profile saveProfile(
      User user, ProfileCreateRequest profileCreateRequest, MultipartFile profileImage
  ) {
    String profileImageUrl = profileImageService.getProfileImageUrl(profileImage);
    Profile profile = profileCreateRequest.toEntity(user, profileImageUrl);
    return profileRepository.save(profile);
  }

  private String updateTokens(HttpServletResponse response, User user) {
    String newAccessToken =
        jwtUtil.createJwt("access", user, Duration.ofMinutes(30).toMillis());
    String newRefreshToken =
        jwtUtil.createJwt("refresh", user, Duration.ofHours(24).toMillis());

    updateRefreshToken(response, user, newRefreshToken, Duration.ofHours(24).toMillis());
    return newAccessToken;
  }

  private void updateRefreshToken(
      HttpServletResponse response, User user, String newRefreshToken, Long expiredMs
  ) {
    LocalDateTime expirationDate = LocalDateTime.now().plusNanos(expiredMs * 1_000_000);
    RefreshToken refreshToken = refreshTokenRepository.findByUser_Id(user.getId())
        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_VERIFICATION_TOKEN));
    refreshToken.updateToken(newRefreshToken, expirationDate);

    response.addCookie(createCookie(newRefreshToken));
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
  }

  private Cookie createCookie(String value) {
    Cookie cookie = new Cookie("refresh", value);
    cookie.setMaxAge(24 * 60 * 60);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    return cookie;
  }
}
