package com.momo.meeting.entity;

import com.momo.common.entity.BaseEntity;
import com.momo.meeting.constant.FoodCategory;
import com.momo.meeting.constant.MeetingStatus;
import com.momo.meeting.dto.create.MeetingCreateRequest;
import com.momo.user.entity.User;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
  private Long locationId;

  @Column(nullable = false)
  private Double latitude;

  @Column(nullable = false)
  private Double longitude;

  @Column(nullable = false)
  private String address;

  @Column(nullable = false)
  private LocalDateTime meetingDateTime;

  @Column(nullable = false)
  private Integer maxCount;

  @Column(nullable = false)
  private Integer approvedCount;

  @ElementCollection // 기본적으로 지연로딩
  @Enumerated(EnumType.STRING)
  private Set<FoodCategory> category;

  @Column(nullable = false, length = 600)
  private String content;

  private String thumbnailUrl;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MeetingStatus meetingStatus;

  public void updateStatus(MeetingStatus newStatus) {
    this.meetingStatus = newStatus;
  }

  public void update(MeetingCreateRequest request) {
    this.title = request.getTitle();
    this.locationId = request.getLocationId();
    this.latitude = request.getLatitude();
    this.longitude = request.getLongitude();
    this.address = request.getAddress();
    this.meetingDateTime = request.getMeetingDateTime();
    this.maxCount = request.getMaxCount();
    this.content = request.getContent();
    this.thumbnailUrl = request.getThumbnailUrl();
    this.category = request.getCategory();
  }

  public boolean isAuthor(Long userId) {
    return this.user.getId().equals(userId);
  }
}