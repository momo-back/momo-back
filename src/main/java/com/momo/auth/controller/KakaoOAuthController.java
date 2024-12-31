package com.momo.auth.controller;

import com.momo.auth.dto.KakaoProfile;
import com.momo.auth.dto.OAuthToken;
import com.momo.user.dto.CustomUserDetails;
import com.momo.user.entity.User;
import com.momo.user.service.UserService;
import com.momo.auth.service.KakaoAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/oauth/kakao")
@RequiredArgsConstructor
public class KakaoOAuthController {

  private final UserService userService;
  private final KakaoAuthService kakaoAuthService;

  @PostMapping("/callback")
  public ResponseEntity<Map<String, Object>> kakaoCallback(@RequestBody Map<String, String> requestBody) {
    String code = requestBody.get("code");
    if (code == null) {
      return ResponseEntity.badRequest().body(Map.of("error", "Authorization code is missing"));
    }

    // 1. 카카오 서버에서 AccessToken 가져오기
    OAuthToken oauthToken = kakaoAuthService.getKakaoToken(code);

    // 2. AccessToken으로 사용자 정보 가져오기
    KakaoProfile kakaoProfile = kakaoAuthService.getKakaoProfile(oauthToken.getAccess_token());

    // 3. 사용자 등록 및 처리
    User kakaoUser = userService.processKakaoUser(kakaoProfile, oauthToken);

    // 4. SecurityContext에 사용자 인증 설정
    CustomUserDetails userDetails = new CustomUserDetails(kakaoUser);
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);

    // 5. 성공 응답
    return ResponseEntity.ok(Map.of(
        "accessToken", oauthToken.getAccess_token(),
        "refreshToken", oauthToken.getRefresh_token(),
        "userId", kakaoUser.getId(),
        "message", "Kakao login successful"
    ));
  }
}
