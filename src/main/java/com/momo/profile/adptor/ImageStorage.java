package com.momo.profile.adptor;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ImageStorage {

  String uploadImage(MultipartFile file);

  MultipartFile getImage(String fileUrl);

  boolean deleteImage(String fileUrl);

  List<String> deleteImageAll(List<String> fileUrls);
}
