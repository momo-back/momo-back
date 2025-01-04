package com.momo.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.momo.auth.dto.KakaoProfile;
import com.momo.auth.dto.OAuthToken;
import com.momo.common.exception.CustomException;
import com.momo.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoAuthService {

  @Value("${kakao.client-id}")
  private String clientId;

  @Value("${kakao.redirect-uri}")
  private String redirectUri;

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  /**
   * 카카오 토큰 요청
   */
  public OAuthToken getKakaoToken(String code) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "authorization_code");
    params.add("client_id", clientId);
    params.add("redirect_uri", redirectUri);
    params.add("code", code);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

    ResponseEntity<String> response = restTemplate.exchange(
        "https://kauth.kakao.com/oauth/token",
        HttpMethod.POST,
        request,
        String.class
    );

    return parseResponse(response.getBody(), OAuthToken.class, ErrorCode.INVALID_KAKAO_RESPONSE);
  }

  /**
   * 카카오 프로필 요청
   */
  public KakaoProfile getKakaoProfile(String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + accessToken);
    headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

    HttpEntity<Void> request = new HttpEntity<>(headers);

    ResponseEntity<String> response = restTemplate.exchange(
        "https://kapi.kakao.com/v2/user/me",
        HttpMethod.POST,
        request,
        String.class
    );

    return parseResponse(response.getBody());
  }

  private KakaoProfile parseResponse(String body) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readValue(body, KakaoProfile.class);
    } catch (Exception e) {
      throw new RuntimeException("카카오 프로필 정보 파싱 실패", e);
    }
  }
  /**
   * 공통 JSON 응답 파싱 메서드
   */
  private <T> T parseResponse(String body, Class<T> type, ErrorCode errorCode) {
    try {
      return objectMapper.readValue(body, type);
    } catch (Exception e) {
      log.error("Failed to parse response: {}", body, e);
      throw new CustomException(ErrorCode.INVALID_KAKAO_RESPONSE);
    }
  }
}
