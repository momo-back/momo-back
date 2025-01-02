package com.momo.auth.controller;

import com.momo.config.JWTUtil;
import com.momo.config.token.repository.RefreshTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequestMapping
public class KakaoDeleteController {

  private final RefreshTokenRepository refreshTokenRepository;
  private final JWTUtil jwtUtil;
  private final RestTemplate restTemplate;

  public KakaoDeleteController(JWTUtil jwtUtil, RefreshTokenRepository refreshTokenRepository, RestTemplate restTemplate) {
    this.jwtUtil = jwtUtil;
    this.refreshTokenRepository = refreshTokenRepository;
    this.restTemplate = restTemplate;
  }

  @DeleteMapping("/api/v1/kakao/delete")
  @Transactional
  public ResponseEntity<?> kakaoDelete(HttpServletRequest request, HttpServletResponse response) {
    // Authorization 헤더에서 Bearer 토큰 추출
    String accessToken = extractAccessTokenFromAuthorizationHeader(request);

    // 엑세스 토큰이 없으면 에러 반환
    if (accessToken == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Access token is missing");
    }

    try {
      // 카카오 회원 탈퇴 처리
      deleteKakaoUser(accessToken);

      // 카카오 회원 탈퇴 후 쿠키 삭제 및 DB에서 Refresh Token 삭제
      clearRefreshCookie(response);
      String email = "kakao_user_email";  // 카카오 이메일을 어떻게 가져올지에 대한 추가 로직 필요
      refreshTokenRepository.deleteByEmail(email);

      return ResponseEntity.ok("카카오 계정 연동이 해제되었습니다.");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("인증에 실패했습니다.");
    }
  }

  private void deleteKakaoUser(String accessToken) {
    String kakaoDeleteUrl = "https://kapi.kakao.com/v1/user/unlink";

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + accessToken);  // 카카오 로그인 시 받은 엑세스 토큰 사용

    HttpEntity<String> entity = new HttpEntity<>(headers);
    try {
      ResponseEntity<String> response = restTemplate.exchange(kakaoDeleteUrl, HttpMethod.POST, entity, String.class);

      // 카카오 API 응답 상태 코드 및 본문을 로그로 출력
      log.debug("Kakao delete API response status: {}", response.getStatusCode());
      log.debug("Kakao delete API response body: {}", response.getBody());

      if (response.getStatusCode() != HttpStatus.OK) {
        throw new RuntimeException("Failed to delete Kakao account. Status: " + response.getStatusCode() + ", Body: " + response.getBody());
      }
    } catch (Exception e) {
      log.error("Error occurred while calling Kakao delete API: ", e);
      throw new RuntimeException("Error occurred while calling Kakao delete API: " + e.getMessage(), e);
    }
  }

  private String extractAccessTokenFromAuthorizationHeader(HttpServletRequest request) {
    String authorizationHeader = request.getHeader("Authorization");
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      return authorizationHeader.substring(7); // "Bearer "를 제외한 토큰 값 추출
    }
    return null;
  }

  // 쿠키에서 Refresh 토큰 추출
  private String extractRefreshTokenFromCookies(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("refresh".equals(cookie.getName())) {
          return cookie.getValue(); // 쿠키에서 refresh 토큰 추출
        }
      }
    }
    return null;
  }

  // 쿠키 삭제 메서드
  private void clearRefreshCookie(HttpServletResponse response) {
    Cookie cookie = new Cookie("refresh", null);
    cookie.setMaxAge(0);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    response.addCookie(cookie);
  }
}
