package com.momo.config.interceptor;

import com.momo.common.exception.CustomException;
import com.momo.common.exception.ErrorCode;
import com.momo.config.constants.ExcludePath;
import com.momo.profile.repository.ProfileRepository;
import com.momo.user.dto.CustomUserDetails;
import java.io.IOException;
import java.util.EnumSet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileValidationInterceptor implements HandlerInterceptor {

  private static final EnumSet<ExcludePath> EXCLUDE_PATHS = EnumSet.allOf(ExcludePath.class);
  private final ProfileRepository profileRepository;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handle
  ) throws IOException {
    String requestURI = request.getRequestURI();
    String method = request.getMethod();

    if (isExcludePath(requestURI, method)) {
      log.info("======================== 인터셉터 패스 ========================");
      return true;
    }

    log.info("======================== 인터셉터 ========================");

    try {
      validateUserProfile(getAuthenticatedUserId());
    } catch (CustomException e) {
      log.info("프로필이 없는 사용자입니다. 프로필 생성 페이지로 리다이렉트합니다.");
      response.sendRedirect("/create/profile"); // 프로필 생성 페이지 url
      return false;
    }
    return true;
  }

  private boolean isExcludePath(String requestURI, String method) {
    return EXCLUDE_PATHS.stream()
        .anyMatch(exclude -> exclude.getPath().equals(requestURI) &&
            exclude.getMethod().equals(HttpMethod.valueOf(method)));
  }

  private void validateUserProfile(Long userId) {
    if (!profileRepository.existsByUser_Id(userId)) {
      throw new CustomException(ErrorCode.NOT_EXISTS_PROFILE);
    }
  }

  private Long getAuthenticatedUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }
    return ((CustomUserDetails) auth.getPrincipal()).getId();
  }
}
