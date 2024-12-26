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

    String accessToken = authorizationHeader.substring(7);
    log.debug("Extracted JWT Token: {}", accessToken);

    try {
      // 토큰 만료 여부 확인
      if (jwtUtil.isExpired(accessToken)) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().print("Access token is expired");
        return;
      }

      // 토큰 타입 확인
      if (!"access".equals(jwtUtil.getTokenType(accessToken))) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().print("Invalid access token");
        return;
      }

      // 이메일로 사용자 정보 조회
      String email = jwtUtil.getEmail(accessToken);
      log.debug("Extracted email from token: {}", email);

      User user = userRepository.findByEmail(email)
          .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

      // 사용자 인증 정보 설정
      CustomUserDetails userDetails = new CustomUserDetails(user);
      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
          userDetails, null, userDetails.getAuthorities());
      org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);

      log.debug("SecurityContext에 사용자 인증 정보 설정 완료: {}", userDetails);

    } catch (ExpiredJwtException e) {
      log.error("토큰 만료: ", e);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().print("Access token expired");
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

  /**
   * 인증이 필요하지 않은 경로인지 확인
   */
  private boolean isExcludedPath(String path) {
    return path.equals("/api/v1/users/login") || path.equals("/api/v1/users/signup") || path.equals("/token/reissue");
  }
}
