package com.momo.profile.persist.entity;

import com.momo.common.entity.BaseEntity;
import com.momo.profile.constant.Gender;
import com.momo.profile.constant.Mbti;
import com.momo.user.entity.User;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Profile extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // TODO: 회원 연결 및 검증 필요
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Gender gender;

  @Column(nullable = false)
  private LocalDate birthDate;

  @Column(nullable = false)
  private String profileImageUrl;

  @Column(nullable = false)
  private String introduction;

  @Column(nullable = false)
  private Mbti mbti;
}
