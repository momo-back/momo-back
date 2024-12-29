package com.momo.user.entity;

import com.momo.config.token.entity.RefreshToken;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email"),
    @UniqueConstraint(columnNames = "phone"),
    @UniqueConstraint(columnNames = "nickname"),
    @UniqueConstraint(columnNames = "verification_token")
})
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id", nullable = false, updatable = false)
  //컬럼 이름 유저 아이디 는 그냥 아이디로 되어야 함.
  private Long id;

  @Column(name = "email", nullable = false, length = 50)
  private String email;

  @Column(name = "password", nullable = false, length = 255)
  private String password;

  @Column(name = "nickname", nullable = false, length = 20)
  private String nickname;

  @Column(name = "phone", nullable = false, length = 20)
  private String phone;

  @Column(name = "verification_token", length = 255) // 추가 필드
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
   * 계정을 활성화하는 비즈니스 로직
   */
  public void verify() {
    this.enabled = true; // 계정 활성화
    this.verificationToken = null; // 인증 완료 시 토큰 제거
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }


  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RefreshToken> refreshTokens = new ArrayList<>();

}
