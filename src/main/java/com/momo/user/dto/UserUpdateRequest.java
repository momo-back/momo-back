package com.momo.user.dto;

import com.momo.profile.constant.Mbti;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
  private String nickname;
  private String phone;
  private String introduction;
  private Mbti mbti;
}
