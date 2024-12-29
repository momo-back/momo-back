package com.momo.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.momo.config.JWTUtil;
import com.momo.config.token.repository.RefreshTokenRepository;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CustomLogoutFilter extends GenericFilterBean {
  private final RefreshTokenRepository refreshTokenRepository;
  private final JWTUtil jwtUtil;

  public CustomLogoutFilter(JWTUtil jwtUtil, RefreshTokenRepository refreshTokenRepository) {
    this.jwtUtil = jwtUtil;
    this.refreshTokenRepository = refreshTokenRepository;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    // 로그아웃 경로와 메서드 검증
    if (!isLogoutRequest(httpRequest)) {
      chain.doFilter(request, response);
      return;
    }

    // Refresh Token 처리
    handleLogout(httpRequest, httpResponse);
  }

  private boolean isLogoutRequest(HttpServletRequest request) {
    return "/api/v1/users/logout".equals(request.getRequestURI()) && "DELETE".equalsIgnoreCase(request.getMethod());
  }

  private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String refresh = extractRefreshToken(request);

    if (refresh == null) {
      sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Refresh token is missing");
      return;
    }

    try {
      if (jwtUtil.isExpired(refresh)) {
        sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Refresh token is expired");
        return;
      }

      if (!"refresh".equals(jwtUtil.getTokenType(refresh))) {
        sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token type");
        return;
      }

      if (!refreshTokenRepository.existsByToken(refresh)) {
        sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Refresh token does not exist");
        return;
      }

      String email = jwtUtil.getEmail(refresh);
      refreshTokenRepository.deleteByEmail(email);

      clearRefreshTokenCookie(response);
      sendSuccessResponse(response, "로그아웃되었습니다.");

    } catch (ExpiredJwtException e) {
      sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Refresh token is expired");
    } catch (Exception e) {
      sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error occurred");
    }
  }

  private String extractRefreshToken(HttpServletRequest request) {
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

  private void clearRefreshTokenCookie(HttpServletResponse response) {
    Cookie cookie = new Cookie("refresh", null);
    cookie.setMaxAge(0);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    response.addCookie(cookie);
  }

  private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
    response.setStatus(statusCode);
    response.setContentType("application/json;charset=UTF-8");

    Map<String, Object> errorData = new HashMap<>();
    errorData.put("status", statusCode);
    errorData.put("error", message);

    String responseBody = new ObjectMapper().writeValueAsString(errorData);
    response.getWriter().write(responseBody);
  }

  private void sendSuccessResponse(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType("application/json;charset=UTF-8");

    Map<String, Object> successData = new HashMap<>();
    successData.put("status", HttpServletResponse.SC_OK);
    successData.put("message", message);

    String responseBody = new ObjectMapper().writeValueAsString(successData);
    response.getWriter().write(responseBody);
  }
}
