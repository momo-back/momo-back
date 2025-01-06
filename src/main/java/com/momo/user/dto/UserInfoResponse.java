package com.momo.user.dto;

import com.momo.profile.constant.Gender;
import com.momo.profile.constant.Mbti;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {
  private String nickname;
  private String phone;
  private String email;
  private Gender gender;
  private LocalDate birth;
  private String profileImageUrl;
  private String introduction;
  private Mbti mbti;
  private String oauthProvider;
}
