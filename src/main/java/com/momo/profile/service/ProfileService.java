package com.momo.profile.service;

import com.momo.profile.constant.Gender;
import com.momo.profile.dto.ProfileCreateRequest;
import com.momo.profile.dto.ProfileCreateResponse;
import com.momo.profile.entity.Profile;
import com.momo.profile.repository.ProfileRepository;
import com.momo.profile.validate.ProfileRequiredValueValidator;
import com.momo.profile.validate.ProfileValidator;
import com.momo.user.dto.CustomUserDetails;
import com.momo.user.entity.User;
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
    profileValidator.validateUser(userId);
    ProfileRequiredValueValidator.validateProfileRequiredValue(gender, birth);
  }
}
