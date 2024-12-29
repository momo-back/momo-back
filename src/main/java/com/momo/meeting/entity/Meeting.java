package com.momo.meeting.entity;

import com.momo.meeting.constant.FoodCategory;
import com.momo.meeting.constant.MeetingStatus;
import com.momo.user.entity.User;
import com.momo.profile.entity.BaseEntity;
import java.time.LocalDateTime;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
@Builder
public class Meeting extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, length = 60)
  private String title;

  @Column(nullable = false)
  private LocalDateTime meetingDateTime;

  @Column(nullable = false)
  private Integer approvedCount;

  @Column(nullable = false)
  private Integer maxCount;

  @Column(nullable = false)
  private Long locationId;

  @ElementCollection // 기본적으로 지연로딩
  @Enumerated(EnumType.STRING)
  private Set<FoodCategory> categories;

  @Column(nullable = false, length = 600)
  private String content;

  private String thumbnailUrl;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MeetingStatus meetingStatus;
}