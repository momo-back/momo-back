package com.momo.participation.entity;

import com.momo.common.entity.BaseEntity;
import com.momo.meeting.entity.Meeting;
import com.momo.participation.constant.ParticipationStatus;
import com.momo.user.entity.User;
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
import javax.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Participation extends BaseEntity {

  @Version
  private Long version;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "meeting_id", nullable = false)
  private Meeting meeting;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ParticipationStatus participationStatus;

  public static Participation createMeetingParticipant(User user, Meeting meeting) {
    return Participation.builder()
        .user(user)
        .meeting(meeting)
        .participationStatus(ParticipationStatus.PENDING)
        .build();
  }

  public void updateStatus(ParticipationStatus newStatus) {
    this.participationStatus = newStatus;
  }

  public boolean isMeetingAuthor(Long authorId) {
    return this.meeting.getUser().getId().equals(authorId);
  }

  public boolean isAuthor(Long userId) {
    return this.user.getId().equals(userId);
  }

  public Long getMeetingId() {
    return this.meeting.getId();
  }

  public boolean isAcceptableStatus() {
    return this.participationStatus == ParticipationStatus.PENDING;
  }
}
