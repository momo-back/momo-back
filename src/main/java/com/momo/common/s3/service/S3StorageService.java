package com.momo.common.s3.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.momo.common.s3.exception.S3ErrorCode;
import com.momo.common.s3.exception.S3Exception;
import com.momo.common.s3.validate.FileValidator;
import com.momo.common.s3.util.FileUtils;
import com.momo.profile.adptor.ImageStorage;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService implements ImageStorage {

  private final AmazonS3 amazonS3;

  @Value("${aws.s3.bucket}")
  private String bucket;

  /**
   * S3 파일 업로드
   *
   * @param imageFile 업로드할 이미지
   * @return 업로드된 이미지 URL 반환
   */
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

  /**
   * S3 파일 삭제
   *
   * @param imageUrl 삭제할 이미지 URL
   * @return 삭제 결과
   */
  @Override
  public boolean deleteImage(String imageUrl) {
    try {
      // URL에서 키 추출
      String key = extractKeyFromUrl(imageUrl);
      if (key == null) {
        log.error("URL에서 key 추출 실패 : {}", imageUrl);
        return false;
      }

      // 객체 존재 여부 확인
      if (!amazonS3.doesObjectExist(bucket, key)) {
        log.warn("S3에 해당 객체가 존재하지 않습니다. Bucket : {}, key: {}", bucket, key);
        return false;
      }

      // S3에서 객체 삭제
      amazonS3.deleteObject(bucket, key);
      log.info("S3에서 객체 삭제 성공 bucket: {}, key: {}", bucket, key);
      return true;

    } catch (AmazonServiceException e) {
      log.error("이미지 삭제 도중 S3 에러 발생 : {}", e.getMessage(), e);
      return false;
    } catch (Exception e) {
      log.error("이미지 삭제 도중 예상치 못한 에러 발생 : {}", e.getMessage(), e);
      return false;
    }
  }

  /**
   * 다중 파일 삭제
   *
   * @param imageUrls 삭제할 이미지 URL 목록
   * @return 삭제 성공한 파일의 URL 목록
   */
  @Override
  public List<String> deleteImageAll(List<String> imageUrls) {
    if (CollectionUtils.isEmpty(imageUrls)) {
      return Collections.emptyList();
    }

    try {
      // URL에서 키 추출
      Map<String, String> urlToKeyMap = imageUrls.stream()
          .filter(StringUtils::hasText) // 각 URL이 null이 아니고, 비어있지 않고, 공백이 아닌지 검사
          .collect(Collectors.toMap(
              url -> url, // URL 자체를 키로 사용
              this::extractKeyFromUrl, // URL에서 S3 키를 추출하여 값으로 사용
              (existing, replacement) -> existing // 키 충돌 발생할 경우 기존 값을 유지
          ));

      // 삭제 요청 객체 생성
      DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucket)
          .withKeys(urlToKeyMap.values().stream()
              .filter(StringUtils::hasText)
              .map(KeyVersion::new)
              .collect(Collectors.toList()));

      // 일괄 삭제 실행
      DeleteObjectsResult result = amazonS3.deleteObjects(deleteObjectsRequest);

      // 삭제 성공한 키 목록 추출
      Set<String> deletedKeys = result.getDeletedObjects().stream()
          .map(DeleteObjectsResult.DeletedObject::getKey)
          .collect(Collectors.toSet());

      // 삭제 성공한 URL 목록 반환
      List<String> successUrls = urlToKeyMap.entrySet().stream()
          .filter(entry -> deletedKeys.contains(entry.getValue()))
          .map(Map.Entry::getKey)
          .collect(Collectors.toList());

      log.info("이미지 {}개 중, {}개 삭제 성공", successUrls.size(), imageUrls.size());
      return successUrls;

    } catch (AmazonServiceException e) {
      log.error("다중 이미지 삭제 도중 S3 에러 발생 : {}", e.getMessage(), e);
      return Collections.emptyList();
    } catch (Exception e) {
      log.error("다중 이미지 삭제 도중 예상치 못한 에러 발생 : {}", e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  /**
   * URL에서 S3 객체 키 추출
   *
   * @param imageUrl S3 이미지 URL
   * @return 추출된 객체 키 또는 null
   */
  private String extractKeyFromUrl(String imageUrl) {
    try {
      if (StringUtils.isEmpty(imageUrl)) {
        return null;
      }

      URL url = new URL(imageUrl); // URL에서 경로 추출
      log.info("URL : {}", url);

      String path = url.getPath();
      log.info("path : {}", path);

      // 버킷 이름이 경로에 포함된 경우 처리
      int bucketIndex = path.indexOf(bucket);
      String key;

      if (bucketIndex >= 0) {
        key = path.substring(bucketIndex + bucket.length() + 1);
      } else {
        // 버킷 이름이 없는 경우 맨 앞의 '/'를 제거
        key = path.startsWith("/") ? path.substring(1) : path;
      }
      log.info("key : {}", path);

      return URLDecoder.decode(key, StandardCharsets.UTF_8); // 디코딩하여 반환

    } catch (Exception e) {
      log.error("Error extracting key from URL: {}", e.getMessage(), e);
      return null;
    }
  }

  private String generateFileUrl(String fileName) {
    return amazonS3.getUrl(bucket, fileName).toString();
  }

  private static String createFilename(MultipartFile file) {
    return "/" + FileUtils.createFileName(file.getOriginalFilename());
  }

  private static ObjectMetadata createObjectMetadata(MultipartFile file) {
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType(file.getContentType());
    metadata.setContentLength(file.getSize());
    return metadata;
  }
}
