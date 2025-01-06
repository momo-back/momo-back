package com.momo.user.controller;

import com.momo.user.dto.EmailRequest;
import com.momo.user.dto.PasswordResetRequest;
import com.momo.user.dto.UserInfoResponse;
import com.momo.user.dto.UserUpdateRequest;
import com.momo.user.service.EmailService;
import com.momo.user.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

  // 비밀번호 재설정 링크 발송
  @PostMapping("/password/change/link-send")
  public ResponseEntity<String> requestPasswordReset(@RequestBody EmailRequest emailRequest) {
    if (emailRequest.getEmail() == null || emailRequest.getEmail().isEmpty()) {
      return ResponseEntity.badRequest().body("이메일 주소를 입력해주세요.");
    }

    userService.sendPasswordResetLink(emailRequest.getEmail());
    return ResponseEntity.ok("비밀번호 재설정 이메일이 발송되었습니다.");
  }

  // 비밀번호 재설정
  @PostMapping("/password/change")
  public ResponseEntity<String> resetPassword(@RequestBody PasswordResetRequest passwordResetRequest) {
    if (passwordResetRequest.getToken() == null || passwordResetRequest.getToken().isEmpty()) {
      return ResponseEntity.badRequest().body("토큰을 입력해주세요.");
    }
    if (passwordResetRequest.getNewPassword() == null || passwordResetRequest.getNewPassword().isEmpty()) {
      return ResponseEntity.badRequest().body("새 비밀번호를 입력해주세요.");
    }

    userService.resetPassword(passwordResetRequest.getToken(), passwordResetRequest.getNewPassword());
    return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
  }

  // 본인 회원정보 조회 (일반 로그인 회원 및 카카오 로그인 회원 모두 처리)
  @GetMapping("/me")
  public ResponseEntity<UserInfoResponse> getUserInfo() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    UserInfoResponse userInfo = userService.getUserInfoByEmail(email);
    return ResponseEntity.ok(userInfo);
  }

  // 회원정보 수정
  @PutMapping("/me")
  public ResponseEntity<String> updateUser(@RequestBody UserUpdateRequest updateRequest) {
    userService.updateUser(updateRequest);
    return ResponseEntity.ok("회원정보가 성공적으로 수정되었습니다.");
  }
}
