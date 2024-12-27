package com.momo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.momo.config.token.entity.RefreshToken;
import com.momo.config.token.repository.RefreshTokenRepository;
import com.momo.user.dto.CustomUserDetails;
import com.momo.user.dto.LoginDTO;
import com.momo.user.entity.User;
import com.momo.user.repository.UserRepository;
import java.io.IOException;
import java.sql.Date;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

  private final JWTUtil jwtUtil;
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;

  public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil,
      RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
    super.setAuthenticationManager(authenticationManager);
    this.jwtUtil = jwtUtil;
    this.userRepository = userRepository;
    this.refreshTokenRepository = refreshTokenRepository;
    this.setFilterProcessesUrl("/api/v1/users/login"); // 로그인 엔드포인트 설정
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
      HttpServletResponse response) {
    try {
      // 요청에서 로그인 정보를 파싱
      ObjectMapper mapper = new ObjectMapper();
      LoginDTO loginDTO = mapper.readValue(request.getReader(), LoginDTO.class);

      String email = loginDTO.getEmail();
      String password = loginDTO.getPassword();

      if (email == null || email.isEmpty()) {
        log.warn("로그인 실패: 이메일이 비어있습니다.");
        throw new IllegalArgumentException("Email is required");
      }

      UsernamePasswordAuthenticationToken authRequest =
          new UsernamePasswordAuthenticationToken(email, password);
      return this.getAuthenticationManager().authenticate(authRequest);
    } catch (IOException e) {
      log.error("로그인 요청 데이터 처리 중 오류 발생: {}", e.getMessage());
      throw new RuntimeException("Invalid login request data", e);
    }
  }

  @Override
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain, Authentication authentication) throws IOException {
    try {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      User user = userDetails.getUser();

      // Access 및 Refresh 토큰 생성
      String accessToken = jwtUtil.createJwt("access", user, Duration.ofMinutes(30).toMillis());
      String refreshToken = jwtUtil.createJwt("refresh", user, Duration.ofHours(24).toMillis());

      log.info("Access Token 생성 완료: {}", accessToken);
      log.info("Refresh Token 생성 완료: {}", refreshToken);

      // Refresh 토큰 저장
      addRefreshToken(user, refreshToken, Duration.ofHours(24).toMillis());

      // 응답에 토큰 추가
      Map<String, String> tokens = new HashMap<>();
      tokens.put("accessToken", accessToken);

      response.addCookie(createCookie("refresh", refreshToken));
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");

      new ObjectMapper().writeValue(response.getWriter(), tokens);
      response.setStatus(HttpStatus.OK.value());
    } catch (Exception e) {
      log.error("로그인 성공 처리 중 오류 발생: {}", e.getMessage());
      handleException(response, e);
    }
  }

  @Override
  protected void unsuccessfulAuthentication(HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException failed) throws IOException {
    log.warn("로그인 실패: {}", failed.getMessage());

    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    Map<String, String> error = new HashMap<>();
    error.put("error", "Authentication failed");

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    new ObjectMapper().writeValue(response.getWriter(), error);
  }

  private void handleException(HttpServletResponse response, Exception ex) throws IOException {
    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    response.setContentType("application/json;charset=UTF-8");

    Map<String, String> responseBody = new HashMap<>();
    responseBody.put("error", ex.getMessage());

    String jsonResponse = new ObjectMapper().writeValueAsString(responseBody);
    response.getWriter().write(jsonResponse);
  }

  private void addRefreshToken(User user, String refreshToken, Long expiredMs) {
    LocalDateTime expirationDate = LocalDateTime.now().plusNanos(expiredMs * 1_000_000);
    RefreshToken refreshTokenEntity = RefreshToken.create(user, refreshToken, expirationDate);
    refreshTokenRepository.save(refreshTokenEntity);
  }

  private Cookie createCookie(String key, String value) {
    Cookie cookie = new Cookie(key, value);
    cookie.setMaxAge(24 * 60 * 60);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    return cookie;
  }
}

