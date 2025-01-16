package com.momo.auth.join.controller;

import com.momo.auth.join.service.VerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class VerificationController {

  private final VerificationService verificationService;

  public VerificationController(VerificationService verificationService) {
    this.verificationService = verificationService;
  }

  @GetMapping("/api/v1/users/signup/verify")
  public ResponseEntity<String> verifyUser(@RequestParam("token") String token) {
    verificationService.verifyUser(token); // 서비스 호출
    return ResponseEntity.ok("이메일 인증이 완료되었습니다!");
  }
}