package com.momo.profile.adptor;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorage {

  String uploadImage(MultipartFile file);

  MultipartFile getImage(String fileKey);

  void deleteImage(String fileKey);
}
