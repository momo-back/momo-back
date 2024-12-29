package com.momo.common.s3.validate;

import com.momo.common.s3.exception.S3ErrorCode;
import com.momo.common.s3.exception.S3Exception;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
public class FileValidator {

  public static void validateFileName(MultipartFile file) {
    if (file.isEmpty()) {
      log.error("파일이 존재하지 않습니다.");
      throw new S3Exception(S3ErrorCode.EMPTY_FILE);
    }

    String filename = file.getOriginalFilename();
    if (filename == null || filename.trim().isEmpty()) {
      log.error("파일명이 유효하지 않습니다.");
      throw new S3Exception(S3ErrorCode.INVALID_FILENAME);
    }
  }
}
