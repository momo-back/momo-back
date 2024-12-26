package com.momo.join.controller;

import com.momo.user.entity.User;
import com.momo.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class VerificationController {

  private final UserRepository userRepository;

  public VerificationController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @GetMapping("/api/v1/user/signup/verify")
  public ResponseEntity<String> verifyUser(@RequestParam("token") String token) {
    User user = userRepository.findByVerificationToken(token)
        .orElseThrow(() -> new RuntimeException("Invalid verification token"));

    user.setEnabled(true); // 계정 활성화
    user.setVerificationToken(null); // 토큰 제거
    userRepository.save(user);

    return ResponseEntity.ok("이메일 인증이 완료되었습니다!");
  }
}