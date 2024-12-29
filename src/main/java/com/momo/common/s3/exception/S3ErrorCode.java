package com.momo.common.s3.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum S3ErrorCode {

  // File
  EMPTY_FILE("파일이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
  INVALID_FILENAME("파일명이 유효하지 않습니다.", HttpStatus.BAD_REQUEST),

  // AWS S3
  AMAZON_SERVICE_ERROR("아마존 서비스 오류", HttpStatus.INTERNAL_SERVER_ERROR),
  INVALID_IO_ERROR("이미지 InputStream 오류", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String message;
  private final HttpStatus status;
}
