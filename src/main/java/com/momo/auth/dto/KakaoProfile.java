package com.momo.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 알 수 없는 속성을 무시
public class KakaoProfile {

  private Long id; // 사용자 고유 ID

  @JsonProperty("connected_at")
  private String connectedAt; // 사용자 연결 시간

  private KakaoAccount kakao_account;

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true) // 알 수 없는 속성을 무시
  public static class KakaoAccount {
    private String email; // 이메일

    @JsonProperty("profile_nickname_needs_agreement")
    private Boolean profileNicknameNeedsAgreement; // 닉네임 동의 필요 여부

    @JsonProperty("profile_image_needs_agreement")
    private Boolean profileImageNeedsAgreement; // 이미지 동의 필요 여부

    private Profile profile;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true) // 알 수 없는 속성을 무시
    public static class Profile {
      private String nickname; // 닉네임

      @JsonProperty("profile_image_url")
      private String profileImageUrl; // 프로필 이미지 URL

      @JsonProperty("thumbnail_image_url")
      private String thumbnailImageUrl; // 썸네일 이미지 URL
    }
  }
}