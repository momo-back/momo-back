package com.momo.config.controller;

import com.momo.config.JWTUtil;
import com.momo.config.token.repository.RefreshTokenRepository;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping
public class LogoutController {

  private final RefreshTokenRepository refreshTokenRepository;
  private final JWTUtil jwtUtil;

  public LogoutController(JWTUtil jwtUtil, RefreshTokenRepository refreshTokenRepository) {
    this.jwtUtil = jwtUtil;
    this.refreshTokenRepository = refreshTokenRepository;
  }

  @DeleteMapping("/api/v1/users/logout")
  @Transactional // 트랜잭션 처리
  public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
    String refreshToken = extractRefreshTokenFromCookies(request);

    // Refresh 토큰 null 체크
    if (refreshToken == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Refresh token is missing");
    }

    // 만료 체크 및 유효성 검증
    try {
      if (jwtUtil.isExpired(refreshToken)) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Refresh token is expired");
      }

      if (!"refresh".equals(jwtUtil.getTokenType(refreshToken))) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token type");
      }
    } catch (ExpiredJwtException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Refresh token is expired");
    }

    // DB에서 Refresh 토큰 존재 여부 확인
    if (!refreshTokenRepository.existsByToken(refreshToken)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Refresh token does not exist");
    }

    // Refresh 토큰 DB에서 제거
    String email = jwtUtil.getEmail(refreshToken);
    refreshTokenRepository.deleteByEmail(email);

    // 쿠키 삭제
    clearRefreshCookie(response);

    return ResponseEntity.ok("로그아웃되었습니다.");
  }

  // 쿠키에서 Refresh 토큰 추출
  private String extractRefreshTokenFromCookies(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("refresh".equals(cookie.getName())) {
          return cookie.getValue();
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
