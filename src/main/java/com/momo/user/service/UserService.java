package com.momo.user.service;

import com.momo.auth.dto.KakaoProfile;
import com.momo.auth.dto.LoginDTO;
import com.momo.auth.dto.OAuthToken;
import com.momo.auth.service.KakaoAuthService;
import com.momo.chat.entity.ChatRoom;
import com.momo.chat.repository.ChatReadStatusRepository;
import com.momo.chat.repository.ChatRoomRepository;
import com.momo.common.exception.CustomException;
import com.momo.common.exception.ErrorCode;
import com.momo.config.JWTUtil;
import com.momo.config.token.entity.RefreshToken;
import com.momo.config.token.repository.RefreshTokenRepository;
import com.momo.image.service.ImageService;
import com.momo.meeting.entity.Meeting;
import com.momo.meeting.repository.MeetingRepository;
import com.momo.notification.repository.NotificationRepository;
import com.momo.participation.entity.Participation;
import com.momo.participation.repository.ParticipationRepository;
import com.momo.profile.entity.Profile;
import com.momo.profile.exception.ProfileErrorCode;
import com.momo.profile.exception.ProfileException;
import com.momo.profile.repository.ProfileRepository;
import com.momo.user.dto.CustomUserDetails;
import com.momo.user.dto.OtherUserInfoProjection;
import com.momo.user.dto.OtherUserInfoResponse;
import com.momo.user.dto.UserInfoProjection;
import com.momo.user.dto.UserInfoResponse;
import com.momo.user.dto.UserUpdateRequest;
import com.momo.user.entity.User;
import com.momo.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
  private final ChatReadStatusRepository chatReadStatusRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final MeetingRepository meetingRepository;
  private final ImageService imageService;
  private final ParticipationRepository participationRepository;
  private final NotificationRepository notificationRepository;

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

    Long userId = user.getId();

    // RefreshToken 삭제
    refreshTokenRepository.deleteByUser(user);

    // Profile 삭제
    profileRepository.findByUser(user).ifPresent(profileRepository::delete);

    // Notification 삭제
    notificationRepository.deleteAllByUser_Id(userId);

    // 탈퇴하려는 사용자가 호스트인 경우 (모임 및 채팅방 삭제)
    List<Meeting> userMeetingsAsHost = meetingRepository.findByUserId(userId);
    for (Meeting meeting : userMeetingsAsHost) {
      // 해당 미팅에 관련된 참여자 삭제
      participationRepository.deleteByMeetingId(meeting.getId());

      // 해당 미팅에 연결된 모든 채팅방 처리
      List<ChatRoom> chatRoomsForMeeting = chatRoomRepository.findByMeeting(meeting);
      for (ChatRoom chatRoom : chatRoomsForMeeting) {
        // 채팅방에서 호스트 제거
        chatRoom.getReader().remove(user);

        // 해당 채팅방의 모든 ChatReadStatus 삭제
        chatReadStatusRepository.deleteByChatRoom(chatRoom);

        // 해당 채팅방 삭제
        chatRoomRepository.delete(chatRoom);
      }
    }

    // 탈퇴하려는 사용자가 참여자일 경우 (참여자 정보 삭제)
    List<Participation> userParticipations = participationRepository.findByUserId(userId);
    for (Participation participation : userParticipations) {
      // 참여자 관련 정보 삭제
      participationRepository.delete(participation);

      // 해당 사용자가 참여한 채팅방이 있을 경우, 해당 채팅방에서 참여자 정보 삭제
      Meeting meeting = participation.getMeeting();
      List<ChatRoom> chatRoomsForMeeting = chatRoomRepository.findByMeeting(meeting);
      for (ChatRoom chatRoom : chatRoomsForMeeting) {
        // 채팅방에서 해당 참여자 제거
        List<User> readers = chatRoom.getReader();  // 채팅방 참여자 리스트 가져오기
        if (readers != null && readers.contains(user)) {
          readers.remove(user);  // 유저를 채팅방에서 제거
        }

        // 해당 채팅방에서의 ChatReadStatus 삭제
        chatReadStatusRepository.deleteByChatRoom_IdAndUser_Id(chatRoom.getId(), user.getId());

        chatRoomRepository.save(chatRoom);
      }
    }

    // 유저가 생성한 Meeting의 카테고리 먼저 삭제
    meetingRepository.deleteCategoriesByUserId(userId);

    // 유저가 생성한 Meeting 삭제 추가
    meetingRepository.deleteByUserId(userId);

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
        .nickname("")  // 닉네임을 빈 문자열("")로 설정
        .phone("")  // 전화번호를 빈 문자열("")로 설정
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


  // 본인 정보 조회
  public UserInfoResponse getUserInfoByEmail(String email) {
    UserInfoProjection projection = userRepository.findUserInfoByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    return UserInfoResponse.builder()
        .nickname(projection.getNickname())
        .phone(projection.getPhone())
        .email(projection.getEmail())
        .gender(projection.getGender())
        .birth(projection.getBirth())
        .profileImageUrl(projection.getProfileImageUrl())
        .introduction(projection.getIntroduction())
        .mbti(projection.getMbti())
        .oauthProvider(projection.isOauthUser() ? "KAKAO" : "LOCAL")
        .build();
  }

  @Transactional
  public void updateUser(UserUpdateRequest updateRequest, MultipartFile profileImage) {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();

    // User 엔티티 조회
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    // Profile 엔티티 조회 및 업데이트
    Profile profile = profileRepository.findByUser(user)
        .orElseThrow(() -> new ProfileException(ProfileErrorCode.NOT_EXISTS_PROFILE));

    // User 엔티티 업데이트
    if (updateRequest.getNickname() != null) {
      user.setNickname(updateRequest.getNickname());
    }
    if (updateRequest.getPhone() != null) {
      user.setPhone(updateRequest.getPhone());
    }

    // Profile 이미지 업데이트 (imageService 사용)
    String profileImageUrl = imageService.uploadImageProcess(profileImage);

    if (profileImageUrl != null) {
      // 기존 프로필 이미지가 있다면 S3에서 삭제
      if (profile.getProfileImageUrl() != null && !profile.getProfileImageUrl().isEmpty()) {
        imageService.deleteImage(profile.getProfileImageUrl());
      }
      // 새 프로필 이미지 업로드 후 저장
      profile.setProfileImageUrl(profileImageUrl);
    } else {
      // 프로필 이미지가 업로드되지 않은 경우, 기본 이미지로 설정하거나 null 유지
      profile.setProfileImageUrl(null); // 기본 이미지가 있으면 여기서 설정
    }

    // Profile 추가 정보 업데이트
    if (updateRequest.getIntroduction() != null) {
      profile.setIntroduction(updateRequest.getIntroduction());
    }
    if (updateRequest.getMbti() != null) {
      profile.setMbti(updateRequest.getMbti());
    }

    log.debug("User and Profile updated successfully for email: {}", email);
  }

  // 다른 사용자 프로필 조회
  @Transactional
  public OtherUserInfoResponse getOtherUserProfile(Long userId) {
    OtherUserInfoProjection projection = userRepository.findOtherUserProfileById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    return OtherUserInfoResponse.builder()
        .nickname(projection.getNickname())
        .gender(projection.getGender())
        .birth(projection.getBirth())
        .mbti(projection.getMbti())
        .introduction(projection.getIntroduction())
        .profileImageUrl(projection.getProfileImageUrl())
        .build();
  }


}