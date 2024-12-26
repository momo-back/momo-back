package com.momo.profile.dto;

import com.momo.profile.constant.Gender;
import com.momo.profile.constant.Mbti;
import com.momo.profile.persist.entity.Profile;
import java.time.LocalDate;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProfileCreateRequest {

  private static final String DEFAULT_INTRODUCTION = "";
  private static final Mbti DEFAULT_MBTI = Mbti.NONE;

  private Gender gender;

  private LocalDate birth;

  private String introduction;

  private Mbti mbti;

  public Profile toEntity(String profileImageKey) {
    return Profile.builder()
        .gender(this.gender)
        .birthDate(this.birth)
        .profileImageUrl(profileImageKey)
        .introduction(this.getIntroductionOrDefault())
        .mbti(this.getMbtiOrDefault())
        .build();
  }

  public String getIntroductionOrDefault() {
    return Optional.ofNullable(this.introduction).orElse(DEFAULT_INTRODUCTION);
  }

  public Mbti getMbtiOrDefault() {
    return Optional.ofNullable(this.mbti).orElse(DEFAULT_MBTI);
  }
}
