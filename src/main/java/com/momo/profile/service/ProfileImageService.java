package com.momo.profile.service;

import com.momo.profile.adptor.ImageStorage;
import com.momo.profile.constant.ImageType;
import com.momo.profile.constant.ProfileConstants;
import com.momo.profile.exception.ProfileErrorCode;
import com.momo.profile.exception.ProfileException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProfileImageService {

  private final ImageStorage imageStorage;

  public String createProfileImageUrl(MultipartFile profileImage) {
    return Optional.ofNullable(profileImage)
        .filter(imageFile -> !imageFile.isEmpty())
        .map(this::uploadImage)
        .orElse(ProfileConstants.DEFAULT_IMAGE_URL);
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
