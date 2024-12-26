package com.momo.user.entity;


import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email"),
    @UniqueConstraint(columnNames = "phone"),
    @UniqueConstraint(columnNames = "nickname")
})
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @Column(name = "email", nullable = false, unique = true, length = 50)
  private String email;

  @Column(name = "password", nullable = false, length = 255)
  private String password;

  @Column(name = "nickname", nullable = false, unique = true, length = 20)
  private String nickname;

  @Column(name = "phone", nullable = false, unique = true, length = 20)
  private String phone;

//  @Column(name = "token", unique = true, length = 255)
//  private String token;

  @Column(name = "verification_token", unique = true, length = 255) // 추가 필드
  private String verificationToken;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "enabled", nullable = false)
  private boolean enabled = false; // 기본값 비활성화

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * 이메일 인증 활성화
   */
  public void activateAccount() {
    this.enabled = true;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * 인증 토큰 설정
   */
  public void setVerificationToken(String token) {
    this.verificationToken = token;
  }

}
