package com.momo.config.interceptor;

import com.momo.common.exception.CustomException;
import com.momo.common.exception.ErrorCode;
import com.momo.config.constants.ExcludePath;
import com.momo.profile.repository.ProfileRepository;
import com.momo.user.dto.CustomUserDetails;
import java.util.EnumSet;
import java.util.Set;
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

  private static final Set<ExcludePath> EXCLUDE_PATHS = EnumSet.allOf(ExcludePath.class);
  private final ProfileRepository profileRepository;

  @Override
  public boolean preHandle(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handle
  ) {
    String requestURI = request.getRequestURI();
    String method = request.getMethod();

    if (isExcludePath(requestURI, method)) {
      log.info("======================== 인터셉터 패스 ========================");
      return true;
    }

    log.info("======================== 인터셉터 ========================");
    validateUserProfile(getAuthenticatedUserId());
    return true;
  }

  private boolean isExcludePath(String requestURI, String method) {
    return EXCLUDE_PATHS.stream()
        .anyMatch(exclude ->
            exclude.getPath().equals(requestURI) &&
                exclude.getMethod().name().equals(method)
        );
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
