package com.momo.user.dto;

import com.momo.profile.constant.Gender;
import com.momo.profile.constant.Mbti;

import java.time.LocalDate;

// 본인 정보 조회용 프로젝션
public interface UserInfoProjection {
  String getNickname();
  String getPhone();
  String getEmail();
  Gender getGender();             // Enum 타입
  LocalDate getBirth();           // LocalDate 타입
  String getProfileImageUrl();
  String getIntroduction();
  Mbti getMbti();                 // Enum 타입
  boolean isOauthUser();          // OAuth 여부
}
