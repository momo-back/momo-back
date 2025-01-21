package com.momo.image.service;

import com.momo.profile.adptor.ImageStorage;
import com.momo.image.constant.ImageType;
import com.momo.profile.exception.ProfileErrorCode;
import com.momo.profile.exception.ProfileException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImageService {

  private final ImageStorage imageStorage;

  // 이미지 파일이 존재하면 S3에 저장하고 url을 반환
  public String uploadImageProcess(MultipartFile image) {
    return Optional.ofNullable(image)
        .filter(imageFile -> !imageFile.isEmpty())
        .map(this::uploadImage)
        .orElse(null);
  }

  public String handleThumbnailUpdate(
      @Nullable String oldThumbnailUrl, @Nullable MultipartFile newThumbnail
  ) {
    boolean hasOldThumbnail = StringUtils.hasText(oldThumbnailUrl);
    String newThumbnailUrl = uploadImageProcess(newThumbnail); // 업로드
    boolean hasNewThumbnail = StringUtils.hasText(newThumbnailUrl);

    // Case 1: 기존 이미지 존재 + 이미지 변경
    if (hasOldThumbnail && hasNewThumbnail && !oldThumbnailUrl.equals(newThumbnailUrl)) {
      imageStorage.deleteImage(oldThumbnailUrl);
      return newThumbnailUrl;
    }

    // Case 2: 기존 이미지 없음 + 이미지 변경
    if (!hasOldThumbnail && hasNewThumbnail) {
      return newThumbnailUrl;  // 새 이미지만 설정하면 됨
    }

    // Case 3: 기존 이미지 존재 + 이미지 제거 (newThumbnailUrl이 null이나 빈 문자열로 왔을 때)
    if (hasOldThumbnail && !hasNewThumbnail) {
      imageStorage.deleteImage(oldThumbnailUrl);
    }
    return null;
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
