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
import com.momo.profile.exception.ProfileErrorCode;
import com.momo.profile.exception.ProfileException;
import com.momo.profile.entity.Profile;
import com.momo.profile.repository.ProfileRepository;
import com.momo.user.entity.User;
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
    User user = createUser();
    String imageUrl = "test-image-url.jpg";
    Profile profile = request.toEntity(user, imageUrl);

    when(profileRepository.existsByUser_Id(user.getId())).thenReturn(false);
    when(profileImageService.getProfileImageUrl(mockImage)).thenReturn(imageUrl);
    when(profileRepository.save(any(Profile.class))).thenReturn(profile);

    // when
    ProfileCreateResponse response = profileService.createProfile(user, request, mockImage);

    // then
    verify(profileRepository).existsByUser_Id(user.getId());
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
    User user = createUser();
    Profile profile = request.toEntity(user, imageUrl);

    when(profileRepository.existsByUser_Id(user.getId())).thenReturn(false);
    when(profileImageService.getProfileImageUrl(null)).thenReturn(imageUrl);
    when(profileRepository.save(any(Profile.class))).thenReturn(profile);

    // when
    ProfileCreateResponse response = profileService.createProfile(user, request, null);

    // then
    verify(profileRepository).existsByUser_Id(user.getId());
    verify(profileRepository).save(any(Profile.class));
    verify(profileImageService).getProfileImageUrl(null);

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
    User user = createUser();
    ProfileCreateRequest request = ProfileCreateRequest.builder()
        .gender(null)
        .birth(LocalDate.of(1990, 1, 1))
        .introduction("자기소개")
        .mbti(Mbti.ENTJ)
        .build();

    // when
    when(profileRepository.existsByUser_Id(user.getId())).thenReturn(false);

    // then
    assertThatThrownBy(() -> profileService.createProfile(user, request, null))
        .isInstanceOf(ProfileException.class);

    verify(profileRepository).existsByUser_Id(user.getId());
  }

  @Test
  @DisplayName("생년월일 누락 시 - 예외 발생")
  void createProfile_WithoutBirthDate_ThrowsException() {
    // given
    User user = createUser();
    ProfileCreateRequest request = ProfileCreateRequest.builder()
        .gender(Gender.MALE)
        .birth(null)
        .introduction("자기소개")
        .mbti(Mbti.ENTJ)
        .build();

    // when
    // then
    assertThatThrownBy(() -> profileService.createProfile(user, request, null))
        .isInstanceOf(ProfileException.class);
  }

  @Test
  @DisplayName("미래 생년월일 입력 시 - 예외 발생")
  void createProfile_WithFutureBirthDate_ThrowsException() {
    // given
    User user = createUser();
    ProfileCreateRequest request = ProfileCreateRequest.builder()
        .gender(Gender.MALE)
        .birth(LocalDate.now().plusDays(1))
        .introduction("자기소개")
        .mbti(Mbti.ENTJ)
        .build();

    // when
    when(profileRepository.existsByUser_Id(user.getId())).thenReturn(false);

    // then
    assertThatThrownBy(() -> profileService.createProfile(user, request, null))
        .isInstanceOf(ProfileException.class)
        .hasFieldOrPropertyWithValue("profileErrorCode", ProfileErrorCode.BIRTH_NOT_FUTURE);
  }

  User createUser() {
    return User.builder()
        .id(1L)
        .email("test@gmail.com")
        .password("testpassword")
        .nickname("testnickname")
        .phone("01012345678")
        .verificationToken(null)
        .enabled(true)
        .build();
  }
}