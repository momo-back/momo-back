package com.momo.profile.dto;

import com.momo.profile.constant.Gender;
import com.momo.profile.constant.Mbti;
import com.momo.profile.persist.entity.Profile;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProfileCreateRequest {
  
  private Gender gender;
  
  private LocalDate birth;

  private String introduction;

  private Mbti mbti;

  public Profile toEntity(String profileImageKey) {
    return Profile.builder()
        .gender(this.gender)
        .birth(this.birth)
        .profileImageUrl(profileImageKey)
        .introduction(this.introduction)
        .mbti(this.mbti)
        .build();
  }
}
