package com.momo.profile.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProfileErrorCode {

  ALREADY_EXISTS("이미 프로필이 존재합니다.", HttpStatus.CONFLICT),
  INVALID_GENDER("성별을 입력해 주세요.", HttpStatus.BAD_REQUEST),
  INVALID_BIRTH("생년월일을 입력해 주세요.", HttpStatus.BAD_REQUEST),
  INVALID_IMAGE_FORMAT("지원하지 않는 이미지 형식입니다.", HttpStatus.BAD_REQUEST);

  private final String message;
  private final HttpStatus status;
}
