package com.momo.auth.join.service;

import com.momo.common.exception.CustomException;
import com.momo.common.exception.ErrorCode;
import com.momo.user.entity.User;
import com.momo.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class VerificationService {

  private final UserRepository userRepository;

  public VerificationService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public void verifyUser(String token) {
    User user = userRepository.findByVerificationToken(token)
        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_VERIFICATION_TOKEN));

    user.verify(); // 계정 활성화 및 인증 토큰 제거
    userRepository.save(user);
  }
}