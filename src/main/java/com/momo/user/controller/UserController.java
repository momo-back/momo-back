package com.momo.user.controller;

import com.momo.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  // 회원 탈퇴
  @DeleteMapping
  public ResponseEntity<String> deleteUser() {
    userService.deleteUser();
    return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
  }

}
