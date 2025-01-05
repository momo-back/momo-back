package com.momo.config.aspect;

import com.momo.common.exception.CustomException;
import com.momo.common.exception.ErrorCode;
import com.momo.profile.repository.ProfileRepository;
import com.momo.user.dto.CustomUserDetails;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class ProfileValidationAspect {

  private final ProfileRepository profileRepository;

  @Before("@annotation(com.momo.common.annotation.RequireProfile)")
  public void validateProfile(JoinPoint joinPoint) {
    CustomUserDetails customUserDetails = extractUserDetails(joinPoint.getArgs());
    if (!profileRepository.existsByUser_Id(customUserDetails.getUser().getId())) {
      throw new CustomException(ErrorCode.NOT_EXISTS_PROFILE);
    }
  }

  private CustomUserDetails extractUserDetails(Object[] args) {
    return Arrays.stream(args)
        .filter(arg -> arg instanceof CustomUserDetails)
        .map(arg -> (CustomUserDetails) arg)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("인증 정보를 찾을 수 없습니다."));
  }
}