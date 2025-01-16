package com.momo.config;

import com.momo.auth.dto.KakaoProfile;
import com.momo.auth.service.KakaoAuthService;
import com.momo.user.dto.CustomUserDetails;
import com.momo.user.entity.User;
import com.momo.user.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

  private final UserRepository userRepository;
  private final JWTUtil jwtUtil;
  private final KakaoAuthService kakaoAuthService;

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

    String token = authorizationHeader.substring(7); // "Bearer " 이후의 토큰 값 추출
    log.debug("Extracted JWT Token: {}", token);

    // 카카오 토큰 처리
    if (isKakaoToken(token)) {
      log.debug("카카오 토큰 처리 중...");

      // 카카오 API를 통해 사용자 정보 조회
      KakaoProfile kakaoProfile = kakaoAuthService.getKakaoProfile(token);
      String email = kakaoProfile.getKakao_account().getEmail();
      log.debug("카카오 프로필에서 추출된 이메일: {}", email);

      // DB에서 사용자 조회
      User user = findUserByEmail(email);

      // 프로필 생성 요청이 아닌 경우에만 프로필 검증 수행
      if (!isProfileCheckSkipPath(requestURI) && !user.isProfileCompleted()) {
        sendErrorResponse(
            response,
            HttpServletResponse.SC_FORBIDDEN,
            "프로필 생성이 필요합니다.");
        return;
      }

      // 사용자 인증 정보 설정
      CustomUserDetails userDetails = new CustomUserDetails(user);
      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
          userDetails, null, userDetails.getAuthorities());
      org.springframework.security.core.context.SecurityContextHolder.getContext()
          .setAuthentication(authentication);

      log.debug("SecurityContext에 카카오 사용자 인증 정보 설정 완료: {}", userDetails);

      filterChain.doFilter(request, response);
      return;
    }

    // JWT 형식 검증 (JWT 토큰이 유효한지 확인)
    if (!isValidJwtFormat(token)) {
      log.error("Invalid JWT format: {}", token);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().print("Invalid JWT token format");
      return;
    }

    // JWT 검증 및 이메일 추출
    try {
      String email = jwtUtil.getEmail(token);
      log.debug("Extracted email from JWT: {}", email);

      User user = findUserByEmail(email);

      // 프로필 완성 여부 확인
      log.info("token : {}", token);
      if (!isProfileCheckSkipPath(requestURI) && !jwtUtil.isProfileCompleted(token)) {
        sendErrorResponse(
            response,
            HttpServletResponse.SC_FORBIDDEN,
            "프로필 생성이 필요합니다."
        );
        return;
      }

      // 사용자 인증 정보 설정
      CustomUserDetails userDetails = new CustomUserDetails(user);
      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
          userDetails, null, userDetails.getAuthorities());
      org.springframework.security.core.context.SecurityContextHolder.getContext()
          .setAuthentication(authentication);

      log.debug("SecurityContext에 사용자 인증 정보 설정 완료: {}", userDetails);
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

    // 필터 체인 계속 실행
    filterChain.doFilter(request, response);
  }

  private User findUserByEmail(String email) {
    return userRepository.findByEmailWithProfile(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
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
    return path.equals("/api/v1/users/login") ||
        path.equals("/api/v1/users/signup") ||
        path.equals("/token/reissue");
  }

  private boolean isProfileCheckSkipPath(String path) {
    return isExcludedPath(path) ||
        path.equals("/api/v1/profiles") ||
        path.equals("/api/v1/users/logout");
  }

  private void sendErrorResponse(HttpServletResponse response, int status, String message)
      throws IOException {
    response.setStatus(status);
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write(String.format(
        "{\"message\": \"%s\", \"code\": \"%s\"}",
        message,
        status == HttpServletResponse.SC_FORBIDDEN ? "PROFILE_REQUIRED" : "UNAUTHORIZED"
    ));
  }

  private boolean isKakaoToken(String token) {
    // 카카오는 JWT 형식이 아니므로, 카카오 토큰은 이 필터에서 처리하지 않음
    return token != null && token.length() > 0 && !isValidJwtFormat(token);
  }
}
