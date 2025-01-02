package com.momo.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.Table;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 알 수 없는 속성을 무시
//@Table(name = "kakao_users")
public class OAuthToken {
  private String access_token;
  private String refresh_token;
  private int expires_in;
  private String token_type;
}