package com.momo.config.interceptor;

import com.momo.common.exception.CustomException;
import com.momo.common.exception.ErrorCode;
import com.momo.profile.repository.ProfileRepository;
import com.momo.user.dto.CustomUserDetails;
import com.momo.user.entity.User;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileValidationInterceptor implements HandlerInterceptor {

  private final ProfileRepository profileRepository;

  @Override
  public boolean preHandle(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handle
  ) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth != null && auth.isAuthenticated()) {
      Long userId = ((CustomUserDetails) auth.getPrincipal()).getId();
      boolean hasProfile = profileRepository.existsByUser_Id(userId);

      log.info("======================== 인터셉터 ========================");
      if (!hasProfile) {
        // TODO: 프로필 생성 페이지로 리다이렉트 시킬지 결정 필요
        throw new CustomException(ErrorCode.NOT_EXISTS_PROFILE);
      }
    }
    return true;
  }
}
