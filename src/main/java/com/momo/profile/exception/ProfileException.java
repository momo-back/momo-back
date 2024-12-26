package com.momo.profile.exception;

import lombok.Getter;

@Getter
public class ProfileException extends RuntimeException {

  private final ProfileErrorCode profileErrorCode;

  public ProfileException(ProfileErrorCode profileErrorCode) {
    super(profileErrorCode.getMessage());
    this.profileErrorCode = profileErrorCode;
  }
}
