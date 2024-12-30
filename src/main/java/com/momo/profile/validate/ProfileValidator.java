package com.momo.profile.validate;

import com.momo.common.exception.CustomException;
import com.momo.common.exception.ErrorCode;
import com.momo.profile.exception.ProfileErrorCode;
import com.momo.profile.exception.ProfileException;
import com.momo.profile.repository.ProfileRepository;
import com.momo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProfileValidator {

  private final UserRepository userRepository;
  private final ProfileRepository profileRepository;

  public void validateUser(Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }

    if (profileRepository.existsByUser_Id(userId)) {
      throw new ProfileException(ProfileErrorCode.DUPLICATE_PROFILE);
    }
  }
}
