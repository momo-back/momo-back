package com.momo.config.token.service;

import com.momo.config.JWTUtil;
import com.momo.config.token.entity.RefreshToken;
import com.momo.config.token.repository.RefreshTokenRepository;
import com.momo.user.entity.User;
import com.momo.user.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import java.time.LocalDateTime;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

  private final JWTUtil jwtUtil;
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;

  public TokenService(JWTUtil jwtUtil, RefreshTokenRepository refreshTokenRepository,
      UserRepository userRepository) {
    this.jwtUtil = jwtUtil;
    this.refreshTokenRepository = refreshTokenRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public ResponseEntity<?> reissueToken(HttpServletRequest request, HttpServletResponse response) {
    String refreshToken = extractRefreshTokenFromCookies(request);

    if (refreshToken == null) {
      return new ResponseEntity<>("Refresh token is missing", HttpStatus.BAD_REQUEST);
    }

    // Refresh Token 만료 여부 확인
    if (isTokenExpired(refreshToken)) {
      return new ResponseEntity<>("Refresh token expired", HttpStatus.UNAUTHORIZED);
    }

    // Refresh Token 타입 확인
    if (!isRefreshToken(refreshToken)) {
      return new ResponseEntity<>("Invalid refresh token type", HttpStatus.BAD_REQUEST);
    }

    // DB에 저장되어 있는지 확인
    if (!refreshTokenRepository.existsByToken(refreshToken)) {
      return new ResponseEntity<>("Refresh token not found in database", HttpStatus.UNAUTHORIZED);
    }

    // 사용자 정보 추출 및 조회
    String email = jwtUtil.getEmail(refreshToken);

    // Optional로 사용자 조회
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found for email: " + email));

    // 새로운 Access 및 Refresh Token 발급
    String newAccessToken = jwtUtil.createJwt("access", user, 600000L); // 10분
    String newRefreshToken = jwtUtil.createJwt("refresh", user, 604800000L); // 7일

    // 기존 Refresh Token 삭제 및 저장
    refreshTokenRepository.deleteByEmail(email);
    saveRefreshToken(user, newRefreshToken, LocalDateTime.now().plusDays(7)); // 새로운 Refresh Token 저장

    // 새 토큰을 응답에 설정
    response.setHeader("access", newAccessToken);
    response.addCookie(createCookie("refresh", newRefreshToken));

    return ResponseEntity.ok(newAccessToken);
  }

  @Transactional
  private void saveRefreshToken(User user, String token, LocalDateTime expiration) {
    // user 검증 로직 추가
    if (user == null || user.getId() == null) {
      throw new IllegalStateException("User or User ID cannot be null");
    }

    RefreshToken refreshToken = RefreshToken.create(user, token, expiration);
    refreshTokenRepository.save(refreshToken);
  }

  // 쿠키에서 Refresh Token 추출
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

  // 토큰 만료 여부 확인
  private boolean isTokenExpired(String token) {
    try {
      return jwtUtil.isExpired(token);
    } catch (ExpiredJwtException e) {
      return true;
    }
  }

  // Refresh Token 타입 확인
  private boolean isRefreshToken(String token) {
    return "refresh".equals(jwtUtil.getTokenType(token));
  }

  // 쿠키 생성 메서드
  private Cookie createCookie(String key, String value) {
    Cookie cookie = new Cookie(key, value);
    cookie.setMaxAge(7 * 24 * 60 * 60); // 7일
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    return cookie;
  }
}
