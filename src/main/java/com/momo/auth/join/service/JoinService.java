package com.momo.auth.join.service;

import com.momo.auth.join.dto.JoinDTO;
import com.momo.auth.join.exception.DuplicateException;
import com.momo.auth.join.exception.ValidationException;
import com.momo.user.entity.User;
import com.momo.user.repository.UserRepository;
import com.momo.user.service.EmailService;
import java.util.UUID;
import javax.mail.MessagingException;
import javax.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class JoinService {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final EmailService emailService;

  public JoinService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, EmailService emailService) {
    this.userRepository = userRepository;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    this.emailService = emailService;
  }

  @Transactional
  public void joinProcess(JoinDTO joinDto) throws MessagingException {
    String email = joinDto.getEmail();
    String password = joinDto.getPassword();
    String nickname = joinDto.getNickname();
    String phone = joinDto.getPhone();

    // 입력값 검증
    validateInput(password);

    // 중복 체크
    checkDuplicate(email, nickname, phone);

    // 인증 토큰 생성
    String token = UUID.randomUUID().toString();

    // User 엔티티 생성
    User user = User.builder()
        .email(email)
        .password(bCryptPasswordEncoder.encode(password))
        .nickname(nickname)
        .phone(phone)
        .enabled(false) // 초기 상태는 비활성화
        .verificationToken(token)
        .build();

    // 유저 저장
    userRepository.save(user);

    // 인증 이메일 발송
    emailService.sendVerificationEmail(email, token);
  }

  private void validateInput(String password) {
    if (password == null || password.trim().isEmpty()) {
      throw new ValidationException("password", "비밀번호는 필수 입력 사항입니다.");
    }
  }

  private void checkDuplicate(String email, String nickname, String phone) {
    if (userRepository.existsByEmail(email)) {
      throw new DuplicateException("email", "이메일이 이미 사용 중입니다.");
    }
    if (userRepository.existsByNickname(nickname)) {
      throw new DuplicateException("nickname", "닉네임이 이미 사용 중입니다.");
    }
    if (userRepository.existsByPhone(phone)) {
      throw new DuplicateException("phone", "전화번호가 이미 사용 중입니다.");
    }
  }
}
