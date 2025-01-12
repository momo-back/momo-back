package com.momo.user.controller;

import com.momo.common.exception.CustomException;
import com.momo.common.exception.ErrorCode;
import com.momo.config.token.repository.RefreshTokenRepository;
import com.momo.user.entity.User;
import com.momo.user.repository.UserRepository;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class LogoutController {

  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;
  private final RestTemplate restTemplate;

  @DeleteMapping("/logout")
  @Transactional
  public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    // Access Token 추출
    String accessToken = extractAccessTokenFromAuthorizationHeader(request);

    // 카카오 로그인 회원 로그아웃 처리
    if (user.isOauthUser() && accessToken != null) {
      log.debug("카카오 회원 로그아웃 처리 중...");
      logoutKakaoUser(accessToken);
    }

    // Refresh Token 삭제
    refreshTokenRepository.deleteByUser(user);

    // Refresh 쿠키 삭제
    clearRefreshCookie(response);

    log.debug("로그아웃 처리 완료: {}", email);
    return ResponseEntity.ok("로그아웃이 완료되었습니다.");
  }

  private void logoutKakaoUser(String accessToken) {
    String kakaoLogoutUrl = "https://kapi.kakao.com/v1/user/logout";

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + accessToken);

    HttpEntity<String> entity = new HttpEntity<>(headers);
    ResponseEntity<String> response = restTemplate.exchange(kakaoLogoutUrl, HttpMethod.POST, entity, String.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new CustomException(ErrorCode.KAKAO_UNLINK_FAILED);
    }

    log.debug("카카오 로그아웃 성공: {}", response.getBody());
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

}
