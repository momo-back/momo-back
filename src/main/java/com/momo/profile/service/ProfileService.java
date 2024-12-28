package com.momo.profile.service;

import com.momo.profile.constant.Gender;
import com.momo.profile.dto.ProfileCreateRequest;
import com.momo.profile.dto.ProfileCreateResponse;
import com.momo.profile.persist.entity.Profile;
import com.momo.profile.persist.reposiroty.ProfileRepository;
import com.momo.profile.validator.ProfileRequiredValueValidator;
import com.momo.profile.validator.ProfileValidator;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

  private final ProfileValidator profileValidator;
  private final ProfileRepository profileRepository;
  private final ProfileImageService profileImageService;

  public ProfileCreateResponse createProfile(
      Long userId,
      ProfileCreateRequest request,
      MultipartFile profileImage
  ) {
    validateForProfileCreation(userId, request.getGender(), request.getBirth());

    String profileImageUrl = profileImageService.createProfileImageUrl(profileImage);
    Profile profile = request.toEntity(profileImageUrl);

    Profile savedProfile = profileRepository.save(profile);
    return ProfileCreateResponse.from(savedProfile);
  }

  private void validateForProfileCreation(Long userId, Gender gender, LocalDate birth) {
    profileValidator.validateUserForProfileCreation(userId);
    ProfileRequiredValueValidator.validateProfileRequiredValue(gender, birth);
  }
}
