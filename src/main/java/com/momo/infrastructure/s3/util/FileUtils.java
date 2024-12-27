package com.momo.infrastructure.s3.util;

import java.util.UUID;

public class FileUtils {

  public static String createFileName(String originalFileName) {
    return UUID.randomUUID() + extractExtension(originalFileName);
  }

  public static String extractExtension(String originalFileName) {
    try {
      return originalFileName.substring(originalFileName.lastIndexOf("."));
    } catch (StringIndexOutOfBoundsException e) {
      throw new RuntimeException("잘못된 형식의 파일입니다.", e);
    }
  }
}
