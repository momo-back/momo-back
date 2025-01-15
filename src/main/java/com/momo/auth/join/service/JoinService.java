package com.momo.auth.join.service;

import com.momo.auth.join.dto.JoinDTO;
import com.momo.auth.join.exception.DuplicateException;
import com.momo.auth.join.exception.ValidationException;
import com.momo.common.exception.CustomException;
import com.momo.common.exception.ErrorCode;
import com.momo.user.entity.User;
import com.momo.user.repository.UserRepository;
import com.momo.user.service.EmailService;
import java.util.concurrent.ConcurrentHashMap;
import javax.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JoinService {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final EmailService emailService;

  // 인증 코드와 이메일 매핑
  private final ConcurrentHashMap<String, JoinDTO> pendingUsers = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, String> verificationCodes = new ConcurrentHashMap<>();

  public void joinProcess(JoinDTO joinDto) throws MessagingException {
    String email = joinDto.getEmail();
    String password = joinDto.getPassword();
    String nickname = joinDto.getNickname();
    String phone = joinDto.getPhone();

    validateInput(password);
    checkDuplicate(email, nickname, phone);

    String verificationCode = generateVerificationCode();
    verificationCodes.put(verificationCode, email); // 코드로 이메일 매핑
    pendingUsers.put(email, joinDto); // 이메일로 JoinDTO 매핑

    emailService.sendVerificationCodeEmail(email, verificationCode);
  }

  @Transactional
  public void verifyCode(String code) {
    String email = verificationCodes.get(code);
    if (email == null) {
      throw new CustomException(ErrorCode.INVALID_VERIFICATION_CODE);
    }

    JoinDTO joinDto = pendingUsers.get(email);
    if (joinDto == null) {
      throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }

    verificationCodes.remove(code); // 인증 성공 시 코드 삭제
    pendingUsers.remove(email); // 인증 성공 시 대기열에서 제거

    // User 엔티티 생성 및 저장
    User user = User.builder()
        .email(email)
        .password(bCryptPasswordEncoder.encode(joinDto.getPassword()))
        .nickname(joinDto.getNickname())
        .phone(joinDto.getPhone())
        .enabled(true)
        .build();

    userRepository.save(user);
  }

  private String generateVerificationCode() {
    return String.valueOf((int) (Math.random() * 900000) + 100000); // 6자리 숫자 생성
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
