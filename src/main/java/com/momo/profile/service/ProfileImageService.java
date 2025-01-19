package com.momo.profile.service;

import com.momo.profile.adptor.ImageStorage;
import com.momo.profile.constant.ImageType;
import com.momo.profile.exception.ProfileErrorCode;
import com.momo.profile.exception.ProfileException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProfileImageService {

  private static final String DEFAULT_IMAGE_URL =
      "https://s3-momo-storage.s3.ap-northeast-2.amazonaws.com/default.jpg";

  private final ImageStorage imageStorage;

  public String getProfileImageUrl(MultipartFile profileImage) {
    return Optional.ofNullable(profileImage)
        .filter(imageFile -> !imageFile.isEmpty())
        .map(this::uploadImage)
        .orElse(DEFAULT_IMAGE_URL);
  }

  private String uploadImage(MultipartFile profileImage) {
    validateImageFormat(profileImage.getContentType(), profileImage.getOriginalFilename());
    return imageStorage.uploadImage(profileImage);
  }

  private void validateImageFormat(String contentType, String filename) {
    if (!ImageType.isSupported(contentType, filename)) {
      throw new ProfileException(ProfileErrorCode.INVALID_IMAGE_FORMAT);
    }
  }
}
