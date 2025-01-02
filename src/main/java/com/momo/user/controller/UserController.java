package com.momo.user.controller;

import com.momo.user.dto.EmailRequest;
import com.momo.user.dto.PasswordResetRequest;
import com.momo.user.service.EmailService;
import com.momo.user.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final EmailService emailService;

  // 회원 탈퇴
  @DeleteMapping
  public ResponseEntity<String> deleteUser() {
    userService.deleteUser();
    return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
  }

  // 비밀번호 재설정 요청
  @PostMapping("/password/change/link-send")
  public ResponseEntity<String> requestPasswordReset(@RequestBody EmailRequest emailRequest) {
    if (emailRequest.getEmail() == null || emailRequest.getEmail().isEmpty()) {
      return ResponseEntity.badRequest().body("이메일 주소를 입력해주세요.");
    }

    try {
      String token = userService.generateResetToken(emailRequest.getEmail());
      emailService.sendPasswordResetEmail(emailRequest.getEmail(), token);
      return ResponseEntity.ok("비밀번호 재설정 이메일이 발송되었습니다.");
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("요청 처리 중 문제가 발생했습니다.");
    }
  }

  @PostMapping("/password/change")
  public ResponseEntity<String> resetPassword(@RequestBody PasswordResetRequest passwordResetRequest) {
    if (passwordResetRequest.getToken() == null || passwordResetRequest.getToken().isEmpty()) {
      return ResponseEntity.badRequest().body("토큰을 입력해주세요.");
    }
    if (passwordResetRequest.getNewPassword() == null || passwordResetRequest.getNewPassword().isEmpty()) {
      return ResponseEntity.badRequest().body("새 비밀번호를 입력해주세요.");
    }

    if (userService.validateResetToken(passwordResetRequest.getToken())) { // 토큰 검증 로직
      userService.resetPassword(passwordResetRequest.getToken(), passwordResetRequest.getNewPassword());
      return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    } else {
      return ResponseEntity.status(400).body("유효하지 않은 토큰입니다.");
    }
  }

}
