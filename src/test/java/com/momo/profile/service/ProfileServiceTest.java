package com.momo.profile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.momo.profile.constant.Gender;
import com.momo.profile.constant.Mbti;
import com.momo.profile.dto.ProfileCreateRequest;
import com.momo.profile.dto.ProfileCreateResponse;
import com.momo.profile.exception.ProfileException;
import com.momo.profile.persist.entity.Profile;
import com.momo.profile.persist.reposiroty.ProfileRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

  @Mock
  private ProfileRepository profileRepository;

  @Mock
  private ProfileImageService profileImageService;

  @InjectMocks
  private ProfileService profileService;

  @Test
  @DisplayName("프로필 생성 (이미지 있는 경우) - 성공")
  void createProfile_WithImage_Success() {
    // given
    ProfileCreateRequest request = createProfileRequest();
    MultipartFile mockImage = mock(MultipartFile.class);
    String imageUrl = "test-image-url.jpg";
    Profile profile = request.toEntity(imageUrl);

    when(profileImageService.getProfileImageUrl(mockImage)).thenReturn(imageUrl);
    when(profileRepository.save(any(Profile.class))).thenReturn(profile);

    // when
    ProfileCreateResponse response = profileService.createProfile(request, mockImage);

    // then
    verify(profileImageService).getProfileImageUrl(mockImage);
    verify(profileRepository).save(any(Profile.class));

    assertThat(response.getGender()).isEqualTo(request.getGender());
    assertThat(response.getBirth()).isEqualTo(request.getBirth());
    assertThat(response.getProfileImageUrl()).isEqualTo(imageUrl);
    assertThat(response.getMbti()).isEqualTo(request.getMbti());
  }

  @Test
  @DisplayName("프로필 생성 (이미지 없는 경우) - 성공")
  void createProfile_WithoutImage_Success() {
    // given
    ProfileCreateRequest request = createProfileRequest();
    String imageUrl = "default-image-url.jpg";
    Profile profile = request.toEntity(imageUrl);

    when(profileImageService.getProfileImageUrl(null)).thenReturn(imageUrl);
    when(profileRepository.save(any(Profile.class))).thenReturn(profile);

    // when
    ProfileCreateResponse response = profileService.createProfile(request, null);

    verify(profileRepository).save(any(Profile.class));
    verify(profileImageService).getProfileImageUrl(null);

    // then
    assertThat(response.getGender()).isEqualTo(request.getGender());
    assertThat(response.getBirth()).isEqualTo(request.getBirth());
    assertThat(response.getProfileImageUrl()).isEqualTo(imageUrl);
    assertThat(response.getMbti()).isEqualTo(request.getMbti());
  }

  private ProfileCreateRequest createProfileRequest() {
    return ProfileCreateRequest.builder()
        .gender(Gender.MALE)
        .birth(LocalDate.of(1990, 1, 1))
        .introduction("자기소개")
        .mbti(Mbti.ENFJ)
        .build();
  }

  @Test
  @DisplayName("성별 누락 시 - 예외 발생")
  void createProfile_WithoutGender_ThrowsException() {
    // given
    ProfileCreateRequest request = ProfileCreateRequest.builder()
        .gender(null)
        .birth(LocalDate.of(1990, 1, 1))
        .introduction("자기소개")
        .mbti(Mbti.ENTJ)
        .build();

    // when
    // then
    assertThatThrownBy(() -> profileService.createProfile(request, null))
        .isInstanceOf(ProfileException.class);
  }

  @Test
  @DisplayName("생년월일 누락 시 - 예외 발생")
  void createProfile_WithoutBirthDate_ThrowsException() {
    // given
    ProfileCreateRequest request = ProfileCreateRequest.builder()
        .gender(Gender.MALE)
        .birth(null)
        .introduction("자기소개")
        .mbti(Mbti.ENTJ)
        .build();

    // when
    // then
    assertThatThrownBy(() -> profileService.createProfile(request, null))
        .isInstanceOf(ProfileException.class);
  }

  @Test
  @DisplayName("미래 생년월일 입력 시 - 예외 발생")
  void createProfile_WithFutureBirthDate_ThrowsException() {
    // given
    ProfileCreateRequest request = ProfileCreateRequest.builder()
        .gender(Gender.MALE)
        .birth(LocalDate.now().plusDays(1))
        .introduction("자기소개")
        .mbti(Mbti.ENTJ)
        .build();

    // when
    // then
    assertThatThrownBy(() -> profileService.createProfile(request, null))
        .isInstanceOf(ProfileException.class);
  }
}