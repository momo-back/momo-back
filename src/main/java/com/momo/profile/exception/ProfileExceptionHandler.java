package com.momo.profile.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice("com.momo.profile")
public class ProfileExceptionHandler {

  @ExceptionHandler(ProfileException.class)
  public ResponseEntity<ProfileErrorResponse> handleProfileException(ProfileException e) {
    log.error("Profile Exception: {}", e.getProfileErrorCode().getMessage());
    return createErrorResponse(e.getProfileErrorCode());
  }

  /**
   * 프로필 생성 요청값의 Enum 들과 날짜 데이터의 예외 처리를 담당하는 메서드입니다.
   *
   * @param e
   * @return 잘못된 요청 관련 메세지가 반환됩니다.
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ProfileErrorResponse> handleHttpMessageNotReadable(
      HttpMessageNotReadableException e
  ) {
    // 예외의 근본 원인이 InvalidFormatException 인지 확인
    if (isInvalidEnumFormat(e)) {
      return createEnumErrorResponse((InvalidFormatException) e.getCause());
    }
    return ResponseEntity.badRequest().body(new ProfileErrorResponse("잘못된 요청 형식입니다."));
  }

  private boolean isInvalidEnumFormat(HttpMessageNotReadableException e) {
    return e.getCause() instanceof InvalidFormatException &&
        ((InvalidFormatException) e.getCause()).getTargetType().isEnum();
  }

  private static ResponseEntity<ProfileErrorResponse> createEnumErrorResponse(
      InvalidFormatException invalidFormat
  ) {
    String enumName = invalidFormat.getTargetType().getSimpleName();
    String invalidValue = invalidFormat.getValue().toString(); // 잘못된 요청값
    String validValues = Arrays.toString(invalidFormat.getTargetType().getEnumConstants());

    return ResponseEntity
        .badRequest()
        .body(new ProfileErrorResponse(
            String.format("잘못된 %s 값입니다: '%s'. 가능한 값: %s",
                enumName, invalidValue, validValues)
        ));
  }

  private ResponseEntity<ProfileErrorResponse> createErrorResponse(ProfileErrorCode errorCode) {
    return ResponseEntity
        .status(errorCode.getStatus())
        .body(new ProfileErrorResponse(errorCode.getMessage()));
  }
}
