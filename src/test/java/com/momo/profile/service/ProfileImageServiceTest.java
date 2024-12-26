package com.momo.profile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.momo.profile.adptor.ImageStorage;
import com.momo.profile.exception.ProfileErrorCode;
import com.momo.profile.exception.ProfileException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ProfileImageServiceTest {

  @Mock
  private ImageStorage imageStorage;

  @InjectMocks
  private ProfileImageService profileImageService;

  private static final String DEFAULT_IMAGE_URL =
      "https://s3-momo-storage.s3.ap-northeast-2.amazonaws.com/default.jpg";

  @Test
  @DisplayName("이미지가 없을 경우 - 기본 이미지 URL 반환")
  void getProfileImageUrl_WithNullImage_ReturnDefaultImageUrl() {
    // given
    // when
    String imageUrl = profileImageService.getProfileImageUrl(null);

    // then
    verifyNoInteractions(imageStorage);
    assertThat(imageUrl).isEqualTo(DEFAULT_IMAGE_URL);
  }

  @Test
  @DisplayName("유효한 이미지 업로드 - 성공")
  void getProfileImageUrl_WithValidImage_Success() {
    // given
    MultipartFile mockImage = mock(MultipartFile.class);
    String imageUrl = "test-image-url.jpg";

    when(mockImage.getContentType()).thenReturn("image/jpg");
    when(mockImage.getOriginalFilename()).thenReturn("test-image-url.jpg");
    when(imageStorage.uploadImage(mockImage)).thenReturn(imageUrl);

    // when
    String response = profileImageService.getProfileImageUrl(mockImage);

    // then
    verify(imageStorage).uploadImage(mockImage);
    assertThat(response).isEqualTo(imageUrl);
  }

  @Test
  @DisplayName("지원하지 않는 이미지 형식 업로드 - 실패")
  void getProfileImageUrl_WithInvalidImageFormat_ThrowException() {
    // given
    MultipartFile mockImage = mock(MultipartFile.class);
    when(mockImage.getContentType()).thenReturn("text-plain/txt");
    when(mockImage.getOriginalFilename()).thenReturn("test-image-url.txt");

    // when & then
    assertThatThrownBy(() -> profileImageService.getProfileImageUrl(mockImage))
        .isInstanceOf(ProfileException.class)
        .hasFieldOrPropertyWithValue("ProfileErrorCode", ProfileErrorCode.INVALID_IMAGE_FORMAT);
  }
}