package com.momo.auth.join.controller;

import com.momo.auth.join.service.JoinService;
import com.momo.auth.join.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class VerificationController {

  private final VerificationService verificationService;
  private final JoinService joinService;


  @PostMapping("/api/v1/users/signup/verify")
  public ResponseEntity<String> verifyCode(@RequestParam String code) {
    joinService.verifyCode(code);
    return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
  }
}
