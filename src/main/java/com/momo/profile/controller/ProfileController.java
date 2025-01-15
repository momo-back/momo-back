package com.momo.profile.controller;

import com.momo.profile.dto.ProfileCreateRequest;
import com.momo.profile.dto.ProfileCreateResponse;
import com.momo.profile.service.ProfileService;
import com.momo.user.dto.CustomUserDetails;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class ProfileController {

  private final ProfileService profileService;

  @PostMapping
  public ResponseEntity<ProfileCreateResponse> createProfile(
      HttpServletResponse response,
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @RequestPart ProfileCreateRequest request,
      @RequestPart(required = false) MultipartFile profileImage
  ) {
    return ResponseEntity.ok(
        profileService.createProfile(response, customUserDetails.getUser(), request, profileImage)
    );
  }
}
