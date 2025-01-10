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
public class OtherUserInfoResponse {
  private String nickname;
  private Gender gender;
  private LocalDate birth;
  private Mbti mbti;
  private String introduction;
}
