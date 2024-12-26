package com.momo.profile.validate;

import com.momo.profile.constant.Gender;
import com.momo.profile.exception.ProfileErrorCode;
import com.momo.profile.exception.ProfileException;
import java.time.LocalDate;

public class ProfileRequiredValueValidator {

  public static void profileRequiredValueValidate(Gender gender, LocalDate birth) {
    genderValidate(gender);
    birthValidate(birth);
  }

  private static void genderValidate(Gender gender) {
    if (gender == null) {
      throw new ProfileException(ProfileErrorCode.INVALID_GENDER);
    }
  }

  private static void birthValidate(LocalDate birth) {
    if (birth == null) {
      throw new ProfileException(ProfileErrorCode.INVALID_BIRTH);
    }
  }
}
