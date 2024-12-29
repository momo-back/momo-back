package com.momo.auth.dto;

import lombok.Data;

@Data
public class KakaoProfile {

  private Long id;
  private KakaoAccount kakao_account;

  @Data
  public static class KakaoAccount {

    private String email;
  }
}