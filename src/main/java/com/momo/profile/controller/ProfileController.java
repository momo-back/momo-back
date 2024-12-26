package com.momo.profile.controller;

import com.momo.profile.dto.ProfileCreateRequest;
import com.momo.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

  private final ProfileService profileService;

  // TODO: 로그인된 사용자만 가능하도록 Authorization 필요
  @PostMapping
  public ResponseEntity<?> createProfile(
      @RequestPart ProfileCreateRequest request,
      @RequestPart(required = false) MultipartFile profileImage
  ) {
    return ResponseEntity.ok(profileService.createProfile(request, profileImage));
  }
}
