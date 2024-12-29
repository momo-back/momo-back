package com.momo.auth.join.controller;

import com.momo.auth.join.dto.JoinDTO;
import com.momo.auth.join.service.JoinService;
import javax.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
public class JoinController {

  private final JoinService joinService;

  public JoinController(JoinService joinService) {

    this.joinService = joinService;

  }

  @PostMapping("/api/v1/users/signup")
  public ResponseEntity<String> join(@RequestBody JoinDTO joinDto) throws MessagingException {
    joinService.joinProcess(joinDto);
    return ResponseEntity.ok("회원가입이 완료되었습니다. 이메일 인증을 마쳐주세요.");
  }

}
