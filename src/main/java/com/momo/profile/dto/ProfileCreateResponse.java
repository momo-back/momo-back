package com.momo.profile.dto;

import com.momo.profile.constant.Gender;
import com.momo.profile.constant.Mbti;
import com.momo.profile.entity.Profile;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProfileCreateResponse {

  private Gender gender;
  private LocalDate birth;
  private String profileImageUrl;
  private String introduction;
  private Mbti mbti;

  public static ProfileCreateResponse from(Profile profile) {
    return ProfileCreateResponse.builder()
        .gender(profile.getGender())
        .birth(profile.getBirthDate())
        .profileImageUrl(profile.getProfileImageUrl())
        .introduction(profile.getIntroduction())
        .mbti(profile.getMbti())
        .build();
  }
}
