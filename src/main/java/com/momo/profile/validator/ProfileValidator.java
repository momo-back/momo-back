package com.momo.profile.validator;

import com.momo.profile.exception.ProfileErrorCode;
import com.momo.profile.exception.ProfileException;
import com.momo.profile.persist.reposiroty.ProfileRepository;
import com.momo.user.exception.UserErrorCode;
import com.momo.user.exception.UserException;
import com.momo.user.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProfileValidator {

  private final UserValidator userValidator;
  private final ProfileRepository profileRepository;

  public void validateUserForProfileCreation(Long userId) {
    if (userValidator.existsById(userId)) {
      throw new UserException(UserErrorCode.USER_NOT_FOUND);
    }
    validateProfileNotExists(userId);
  }

  private void validateProfileNotExists(Long userId) {
    if (profileRepository.existsByUserId(userId)) {
      throw new ProfileException(ProfileErrorCode.ALREADY_EXISTS);
    }
  }
}
