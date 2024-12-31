package com.momo.user.entity;

import com.momo.config.token.entity.RefreshToken;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Setter
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
  private Long id;

  @Column(name = "email", nullable = false, length = 50)
  private String email;

  @Column(name = "password", nullable = false, length = 255)
  private String password;

  @Column(name = "nickname", nullable = false, length = 200)
  private String nickname;

  @Column(name = "phone", nullable = true, length = 20) // nullable = true 설정
  private String phone = ""; // 기본값 설정

  @Column(name = "verification_token", length = 255) // 추가 필드
  private String verificationToken;

  @Column(name = "access_token", length = 255)
  private String accessToken;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "enabled", nullable = false)
  private boolean enabled = false; // 기본값 비활성화

  @Column(nullable = false)
  private boolean oauthUser = false; // 기본값은 false

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

  public boolean isOauthUser() {
    return oauthUser;
  }

  public void setOauthUser(boolean oauthUser) {
    this.oauthUser = oauthUser;
  }


  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RefreshToken> refreshTokens = new ArrayList<>();

  public List<GrantedAuthority> getAuthorities() {
    return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
  }


}
