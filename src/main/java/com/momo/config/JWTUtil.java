package com.momo.config;

import com.momo.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JWTUtil {

  private final Key key;

  // 생성자: Base64로 인코딩된 시크릿을 디코딩해서 사용
  public JWTUtil(@Value("${spring.jwt.secret}") String secret) {
    byte[] keyBytes = Decoders.BASE64.decode(secret);  // Base64 디코딩
    this.key = Keys.hmacShaKeyFor(keyBytes); // 키 생성
  }

  private Claims getClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  // Email 추출
  public String getEmail(String token) {
    Claims claims = getClaims(token);
    return claims.get("email", String.class);
  }

  // 토큰 타입 추출
  public String getTokenType(String token) {
    Claims claims = getClaims(token);
    return claims.get("tokenType", String.class);
  }

  // Role 추출 (기본값 "ROLE_USER" 반환)
  public String getRole(String token) {
    Claims claims = getClaims(token);
    return claims.get("role", String.class); // 토큰에 저장된 role 반환
  }

  // 프로필 상태 확인
  public boolean isProfileCompleted(String token) {
    Claims claims = getClaims(token);
    return claims.get("profileCompleted", Boolean.class);
  }

  // 토큰 만료 여부 확인
  public boolean isExpired(String token) {
    try {
      Claims claims = getClaims(token);
      return claims.getExpiration().before(new Date());
    } catch (ExpiredJwtException e) {
      return true;
    }
  }

  // 토큰 생성
  public String createJwt(String tokenType, User user, Long expiredMs) {
    Date now = new Date();
    Date expiration = new Date(now.getTime() + expiredMs);

    // 기본적으로 "ROLE_USER"로 설정
    String role = "ROLE_USER";

    return Jwts.builder()
        .claim("tokenType", tokenType)
        .claim("email", user.getEmail()) // 이메일 기반으로 변경
        .claim("role", role) // 기본 역할 추가
        .claim("profileCompleted", user.isProfileCompleted()) // 프로필 상태 추가
        .setIssuedAt(now)
        .setExpiration(expiration)
        .signWith(key)
        .compact();
  }
}

