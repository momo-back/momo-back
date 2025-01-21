package com.momo.profile.adptor;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ImageStorage {

  String uploadImage(MultipartFile file);

  MultipartFile getImage(String fileKey);

  boolean deleteImage(String fileKey);

  List<String> deleteImageAll(List<String> images);
}
