/*
package com.momo.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.momo.auth.dto.KakaoProfile;
import com.momo.auth.dto.OAuthToken;
import com.momo.user.entity.User;
import com.momo.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;

import java.util.Map;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/v1/oauth/kakao")
public class KakaoOAuthController {

  //@Value("${lwj.key}")
  private String lwjKey;

  @Autowired
  private UserService userService;

  @Autowired
  private AuthenticationManager authenticationManager;

  @PostMapping("/callback")
  public ResponseEntity<Map<String, Object>> kakaoCallbackPost(@RequestBody Map<String, String> requestBody) {
    String code = requestBody.get("code");
    if (code == null) {
      return ResponseEntity.badRequest().body(Map.of("error", "Authorization code is missing"));
    }

    try {
      OAuthToken oauthToken = requestKakaoToken(code);
      KakaoProfile kakaoProfile = requestKakaoProfile(oauthToken.getAccess_token());
      User kakaoUser = userService.processKakaoUser(kakaoProfile, oauthToken);

      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(kakaoUser.getEmail(), lwjKey)
      );
      SecurityContextHolder.getContext().setAuthentication(authentication);

      return ResponseEntity.ok(Map.of(
          "accessToken", oauthToken.getAccess_token(),
          "refreshToken", oauthToken.getRefresh_token(),
          "userId", kakaoUser.getId()
      ));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
          "error", "An error occurred during Kakao login",
          "details", e.getMessage()
      ));
    }
  }

  private OAuthToken requestKakaoToken(String code) {
    try {
      RestTemplate rt = new RestTemplate();
      HttpHeaders headers = new HttpHeaders();
      headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

      MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
      params.add("grant_type", "authorization_code");
      params.add("client_id", "b92bd1084df8faa4f3653a1711d253df");
      params.add("redirect_uri", "http://localhost:8080/login/oauth2/code/kakao");
      params.add("code", code);

      HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);

      ResponseEntity<String> response = rt.exchange(
          "https://kauth.kakao.com/oauth/token",
          HttpMethod.POST,
          kakaoTokenRequest,
          String.class
      );

      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readValue(response.getBody(), OAuthToken.class);
    } catch (Exception e) {
      throw new RuntimeException("Failed to request Kakao token", e);
    }
  }

  private KakaoProfile requestKakaoProfile(String accessToken) {
    try {
      RestTemplate rt = new RestTemplate();
      HttpHeaders headers = new HttpHeaders();
      headers.add("Authorization", "Bearer " + accessToken);
      headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

      HttpEntity<Void> kakaoProfileRequest = new HttpEntity<>(headers);

      ResponseEntity<String> response = rt.exchange(
          "https://kapi.kakao.com/v2/user/me",
          HttpMethod.POST,
          kakaoProfileRequest,
          String.class
      );

      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readValue(response.getBody(), KakaoProfile.class);
    } catch (Exception e) {
      throw new RuntimeException("Failed to request Kakao profile", e);
    }
  }
}
*/
