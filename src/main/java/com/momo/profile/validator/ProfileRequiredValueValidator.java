package com.momo.profile.validation;

import com.momo.profile.constant.Gender;
import com.momo.profile.exception.ProfileErrorCode;
import com.momo.profile.exception.ProfileException;
import java.time.LocalDate;

public class ProfileRequiredValueValidator {

  public static void validateProfileRequiredValue(Gender gender, LocalDate birth) {
    validateGender(gender);
    validateBirth(birth);
  }

  private static void validateGender(Gender gender) {
    if (gender == null) {
      throw new ProfileException(ProfileErrorCode.INVALID_GENDER);
    }
  }

  private static void validateBirth(LocalDate birth) {
    if (birth == null) {
      throw new ProfileException(ProfileErrorCode.INVALID_BIRTH);
    }
    if (birth.isAfter(LocalDate.now())) {
      throw new ProfileException(ProfileErrorCode.INVALID_BIRTH);
    }
  }
}
