package com.momo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.momo.common.exception.CustomException;
import com.momo.common.exception.ErrorCode;
import com.momo.config.constants.ValidatePath;
import com.momo.profile.exception.ProfileErrorCode;
import com.momo.profile.exception.ProfileErrorResponse;
import com.momo.profile.exception.ProfileException;
import com.momo.profile.repository.ProfileRepository;
import com.momo.user.dto.CustomUserDetails;
import java.io.IOException;
import java.util.EnumSet;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileValidationFilter extends OncePerRequestFilter {

  private static final EnumSet<ValidatePath> VALIDATE_PATHS = EnumSet.allOf(ValidatePath.class);
  private final ProfileRepository profileRepository;
  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
  ) throws ServletException, IOException {
    String requestURI = request.getRequestURI();
    String method = request.getMethod();

    if (!needsValidation(requestURI, method)) {
      filterChain.doFilter(request, response);
      log.info("프로필 생성 검증 패스");
      return;
    }
    log.info("프로필 생성 검증");

    try {
      validateUserProfile(getAuthenticatedUserId());
      filterChain.doFilter(request, response);
    } catch (ProfileException e) {
      response.setStatus(HttpStatus.FORBIDDEN.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setCharacterEncoding("UTF-8");

      ProfileErrorResponse errorResponse =
          new ProfileErrorResponse(e.getProfileErrorCode().getMessage());
      response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
    }
  }

  private boolean needsValidation(String requestURI, String method) {
    return VALIDATE_PATHS.stream()
        .anyMatch(path ->
            pathMatcher.match(path.getPath(), requestURI) &&
                path.getMethod().matches(method)
        );
  }

  private void validateUserProfile(Long userId) {
    if (!profileRepository.existsByUser_Id(userId)) {
      throw new ProfileException(ProfileErrorCode.NOT_EXISTS_PROFILE);
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
