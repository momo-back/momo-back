package com.momo.user.validator;

import com.momo.user.entity.User;
import com.momo.user.exception.UserErrorCode;
import com.momo.user.exception.UserException;
import com.momo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidator {

  private final UserRepository userRepository;

  public boolean existsById(Long userId) {
    return userRepository.existsById(userId);
  }

  public User findUserById(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
  }
}
