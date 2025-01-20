package com.momo.image.service;

import com.momo.profile.adptor.ImageStorage;
import com.momo.image.constant.ImageType;
import com.momo.profile.exception.ProfileErrorCode;
import com.momo.profile.exception.ProfileException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImageService {

  private final ImageStorage imageStorage;

  // 이미지 파일이 존재하면 S3에 저장하고 url을 반환
  public String getImageUrl(MultipartFile image) {
    return Optional.ofNullable(image)
        .filter(imageFile -> !imageFile.isEmpty())
        .map(this::uploadImage)
        .orElse(null);
  }

  // 이미지 S3에 업로드
  private String uploadImage(MultipartFile profileImage) {
    validateImageFormat(profileImage.getContentType(), profileImage.getOriginalFilename());
    return imageStorage.uploadImage(profileImage);
  }

  // 지원하는 포맷인지 검증
  private void validateImageFormat(String contentType, String filename) {
    if (!ImageType.isSupported(contentType, filename)) {
      throw new ProfileException(ProfileErrorCode.INVALID_IMAGE_FORMAT);
    }
  }
}
