package com.momo.config;

import com.momo.user.dto.CustomUserDetails;
import com.momo.user.entity.User;
import com.momo.user.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Slf4j
public class JWTFilter extends OncePerRequestFilter {

  private final UserRepository userRepository;
  private final JWTUtil jwtUtil;

  public JWTFilter(UserRepository userRepository, JWTUtil jwtUtil) {
    this.userRepository = userRepository;
    this.jwtUtil = jwtUtil;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws IOException, ServletException {
    log.debug("JWTFilter 실행 중");

    // 인증이 필요하지 않은 요청 URI 확인
    String requestURI = request.getRequestURI();
    if (isExcludedPath(requestURI)) {
      filterChain.doFilter(request, response);
      return;
    }

    // Authorization 헤더에서 Bearer 토큰 추출
    String authorizationHeader = request.getHeader("Authorization");
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authorizationHeader.substring(7);  // "Bearer "를 제외한 토큰 값 추출
    log.debug("Extracted JWT Token: {}", token);

    // 카카오는 JWT 형식이 아니므로 카카오 토큰 처리를 하지 않음
    if (isKakaoToken(token)) {
      // 카카오는 JWT 형식이 아니므로 별도로 처리 (JWT 파싱을 건너뛰고 진행)
      log.debug("Kakao token detected, skipping JWT parsing.");
      filterChain.doFilter(request, response);
      return;
    }

    // JWT 형식 검증 (엑세스 토큰 또는 리프레시 토큰에 대해 검증)
    if (!isValidJwtFormat(token)) {
      log.error("Invalid JWT format: {}", token);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().print("Invalid JWT token format");
      return;
    }

    try {
      // 엑세스 토큰 또는 리프레시 토큰에 대한 만료 체크 및 타입 체크
      if (isRefreshToken(token)) {
        if (jwtUtil.isExpired(token)) {
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          response.getWriter().print("Refresh token expired");
          return;
        }
      } else {
        if (jwtUtil.isExpired(token)) {
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          response.getWriter().print("Access token expired");
          return;
        }

        if (!"access".equals(jwtUtil.getTokenType(token))) {
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          response.getWriter().print("Invalid access token");
          return;
        }
      }
    } catch (ExpiredJwtException e) {
      log.error("토큰 만료: ", e);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().print("Expired JWT token");
      return;
    } catch (JwtException e) {
      log.error("JWT 오류: ", e);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().print("Invalid JWT token");
      return;
    }

    // 이메일로 사용자 정보 조회
    String email = jwtUtil.getEmail(token);
    log.debug("Extracted email from token: {}", email);

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

    // 사용자 인증 정보 설정
    CustomUserDetails userDetails = new CustomUserDetails(user);
    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
        userDetails, null, userDetails.getAuthorities());
    org.springframework.security.core.context.SecurityContextHolder.getContext()
        .setAuthentication(authentication);

    log.debug("SecurityContext에 사용자 인증 정보 설정 완료: {}", userDetails);

    // 필터 체인 계속 실행
    filterChain.doFilter(request, response);
  }

  private boolean isValidJwtFormat(String token) {
    // JWT 형식 확인: "header.payload.signature"
    return token != null && token.split("\\.").length == 3;
  }

  // 리프레시 토큰 확인
  private boolean isRefreshToken(String token) {
    try {
      String tokenType = jwtUtil.getTokenType(token);
      return "refresh".equals(tokenType); // "refresh" 타입이면 리프레시 토큰
    } catch (JwtException e) {
      return false;
    }
  }

  private boolean isExcludedPath(String path) {
    return path.equals("/api/v1/users/login") || path.equals("/api/v1/users/signup")
        || path.equals("/token/reissue");
  }

  private boolean isKakaoToken(String token) {
    // 카카오는 JWT 형식이 아니므로, 카카오 토큰은 이 필터에서 처리하지 않음
    return token != null && token.length() > 0 && !isValidJwtFormat(token);
  }
}
