package com.momo.auth.dto;

import lombok.Data;

@Data
public class OAuthToken {
  private String access_token;
  private String refresh_token;
  private int expires_in;
}