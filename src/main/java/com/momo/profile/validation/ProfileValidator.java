package com.momo.profile.validation;

import com.momo.profile.exception.ProfileErrorCode;
import com.momo.profile.exception.ProfileException;
import com.momo.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProfileValidator {

  private final ProfileRepository profileRepository;

  public void validateHasProfile(Long userId) {
    if (profileRepository.existsByUser_Id(userId)) {
      throw new ProfileException(ProfileErrorCode.DUPLICATE_PROFILE);
    }
  }
}
