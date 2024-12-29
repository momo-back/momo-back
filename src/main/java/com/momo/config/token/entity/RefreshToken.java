package com.momo.config.token.entity;

import com.momo.user.entity.User;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "refresh_token")
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user; // 사용자와 연관

  @Column(name = "token", nullable = false, unique = true)
  private String token; // Refresh Token 값

  @Column(name = "expiration", nullable = false)
  private LocalDateTime expiration; // 토큰 만료 시간

  // 생성자
  private RefreshToken(User user, String token, LocalDateTime expiration) {
    this.user = user;
    this.token = token;
    this.expiration = expiration;
  }

  // 필요한 생성자 추가
  public RefreshToken(User user, String token) {
    this.user = user;
    this.token = token;
  }

  // 정적 팩토리 메서드
  public static RefreshToken create(User user, String token, LocalDateTime expiration) {
    return new RefreshToken(user, token, expiration);
  }

  @PrePersist
  protected void onCreate() {
    if (this.expiration == null) {
      this.expiration = LocalDateTime.now().plusDays(7); // 기본 만료 시간 설정 (7일)
    }
  }
}
