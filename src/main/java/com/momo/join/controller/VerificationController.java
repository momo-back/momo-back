package com.momo.join.controller;

import com.momo.common.exception.CustomException;
import com.momo.common.exception.ErrorCode;
import com.momo.join.service.VerificationService;
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