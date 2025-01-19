package com.momo.profile.service;

import com.momo.profile.constant.Gender;
import com.momo.profile.dto.ProfileCreateRequest;
import com.momo.profile.dto.ProfileCreateResponse;
import com.momo.profile.entity.Profile;
import com.momo.profile.exception.ProfileErrorCode;
import com.momo.profile.exception.ProfileException;
import com.momo.profile.repository.ProfileRepository;
import com.momo.user.entity.User;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

  private final ProfileRepository profileRepository;
  private final ProfileImageService profileImageService;

  @Transactional
  public ProfileCreateResponse createProfile(
      User user,
      ProfileCreateRequest request,
      MultipartFile profileImage
  ) {
    validateForProfileCreation(user.getId(), request.getGender(), request.getBirth());

    String profileImageUrl = profileImageService.getProfileImageUrl(profileImage);
    Profile profile = request.toEntity(user, profileImageUrl);

    Profile savedProfile = profileRepository.save(profile);
    return ProfileCreateResponse.from(savedProfile);
  }

  private void validateForProfileCreation(Long userId, Gender gender, LocalDate birth) {
    validateHasProfile(userId);
    validateProfileRequiredValue(gender, birth);
  }

  private  void validateHasProfile(Long userId) {
    if (profileRepository.existsByUser_Id(userId)) {
      throw new ProfileException(ProfileErrorCode.DUPLICATE_PROFILE);
    }
  }

  private static void validateProfileRequiredValue(Gender gender, LocalDate birth) {
    validateGender(gender);
    validateBirth(birth);
  }

  private static void validateGender(Gender gender) {
    if (gender == null) {
      throw new ProfileException(ProfileErrorCode.INVALID_GENDER);
    }
  }

  private static void validateBirth(LocalDate birth) {
    if (birth == null) {
      throw new ProfileException(ProfileErrorCode.INVALID_BIRTH);
    }
    if (birth.isAfter(LocalDate.now())) {
      throw new ProfileException(ProfileErrorCode.BIRTH_NOT_FUTURE);
    }
  }
}
