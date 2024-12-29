package com.momo.common.s3.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.momo.common.s3.exception.S3ErrorCode;
import com.momo.common.s3.exception.S3Exception;
import com.momo.common.s3.validate.FileValidator;
import com.momo.common.s3.util.FileUtils;
import com.momo.profile.adptor.ImageStorage;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService implements ImageStorage {

  private static final String PROFILE_IMAGE_PATH_PREFIX = "profile";

  private final AmazonS3 amazonS3;

  @Value("${aws.s3.bucket}")
  private String bucket;

  @Override
  public String uploadImage(MultipartFile imageFile) {
    FileValidator.validateFileName(imageFile);
    String filename = createFilename(imageFile);

    ObjectMetadata metadata = createObjectMetadata(imageFile);
    try {
      amazonS3.putObject(bucket, filename, imageFile.getInputStream(), metadata);

      log.info("이미지 업로드 성공");
      return generateFileUrl(filename);

    } catch (AmazonServiceException e) {
      throw new S3Exception(S3ErrorCode.AMAZON_SERVICE_ERROR);
    } catch (IOException e) {
      throw new S3Exception(S3ErrorCode.INVALID_IO_ERROR);
    }
  }

  @Override
  public MultipartFile getImage(String fileKey) {
    return null;
  }

  @Override
  public void deleteImage(String fileKey) {
    amazonS3.deleteObject(bucket, fileKey);
  }

  private String generateFileUrl(String fileName) {
    return amazonS3.getUrl(bucket, fileName).toString();
  }

  private static String createFilename(MultipartFile file) {
    String fileName = FileUtils.createFileName(file.getOriginalFilename());
    return PROFILE_IMAGE_PATH_PREFIX + "/" + fileName;
  }

  private static ObjectMetadata createObjectMetadata(MultipartFile file) {
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType(file.getContentType());
    metadata.setContentLength(file.getSize());
    return metadata;
  }
}
