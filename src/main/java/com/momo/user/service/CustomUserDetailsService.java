package com.momo.user.service;

import com.momo.user.dto.CustomUserDetails;
import com.momo.user.entity.User;
import com.momo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    log.info("Authenticating user with email: {}", email);

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

    log.info("User found: {}", user);

    // OAuth 사용자: 비밀번호 검증 생략
    if (user.isOauthUser()) {
      return new CustomUserDetails(user); // 비밀번호 없이 생성
    }

    // 일반 사용자
    return new CustomUserDetails(user, user.getPassword());
  }
}