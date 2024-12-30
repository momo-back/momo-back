package com.momo.profile.validate;

import com.momo.profile.constant.Gender;
import com.momo.profile.exception.ProfileErrorCode;
import com.momo.profile.exception.ProfileException;
import java.time.LocalDate;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProfileRequiredValueValidator {

  public void validateProfileRequiredValue(Gender gender, LocalDate birth) {
    validateGender(gender);
    validateBirth(birth);
  }

  private void validateGender(Gender gender) {
    if (gender == null) {
      throw new ProfileException(ProfileErrorCode.INVALID_GENDER);
    }
  }

  private void validateBirth(LocalDate birth) {
    if (birth == null) {
      throw new ProfileException(ProfileErrorCode.INVALID_BIRTH);
    }
    if (birth.isAfter(LocalDate.now())) {
      throw new ProfileException(ProfileErrorCode.INVALID_BIRTH);
    }
  }
}
