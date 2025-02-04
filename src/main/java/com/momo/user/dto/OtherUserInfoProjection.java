package com.momo.user.dto;

import com.momo.profile.constant.Gender;
import com.momo.profile.constant.Mbti;

import java.time.LocalDate;

// 다른 사용자 프로필 조회용 프로젝션
public interface OtherUserInfoProjection {
  String getNickname();
  Gender getGender();             // Enum 타입
  LocalDate getBirth();           // LocalDate 타입
  Mbti getMbti();                 // Enum 타입
  String getIntroduction();
  String getProfileImageUrl();
}
