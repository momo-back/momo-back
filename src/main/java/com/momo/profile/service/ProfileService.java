package com.momo.profile.service;

import com.momo.profile.dto.ProfileCreateRequest;
import com.momo.profile.dto.ProfileCreateResponse;
import com.momo.profile.entity.Profile;
import com.momo.profile.repository.ProfileRepository;
import com.momo.profile.validate.ProfileRequiredValueValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

  private final ProfileRepository profileRepository;
  private final ProfileImageService profileImageService;

  // TODO: 회원 ID 매개변수로 받아야 함
  public ProfileCreateResponse createProfile(
      ProfileCreateRequest request, MultipartFile profileImage
  ) {
    // TODO: 회원 존재 여부 검증
    // TODO: 이미 프로필이 존재하는지 여부 검증

    ProfileRequiredValueValidator.profileRequiredValueValidate(
        request.getGender(), request.getBirth()
    );

    String profileImageUrl = profileImageService.getProfileImageUrl(profileImage);
    Profile profile = request.toEntity(profileImageUrl);

    Profile savedProfile = profileRepository.save(profile);
    return ProfileCreateResponse.from(savedProfile);
  }
}
