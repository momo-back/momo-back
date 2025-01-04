package com.momo.common.aspect;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.momo.common.exception.CustomException;
import com.momo.common.exception.ErrorCode;
import com.momo.config.aspect.ProfileValidationAspect;
import com.momo.profile.repository.ProfileRepository;
import com.momo.user.dto.CustomUserDetails;
import com.momo.user.entity.User;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProfileValidationAspectTest {

  @Mock
  private ProfileRepository profileRepository;

  @InjectMocks
  private ProfileValidationAspect profileValidationAspect;

  @Test
  @DisplayName("프로필이 없는 경우 - CustomException 발생")
  void throwExceptionWhenProfileNotExists() {
    // given
    Long userId = 1L;
    CustomUserDetails userDetails = createCustomUserDetails(userId);

    JoinPoint joinPoint = mock(JoinPoint.class);

    when(joinPoint.getArgs()).thenReturn(new Object[]{userDetails});
    when(profileRepository.existsByUser_Id(userId)).thenReturn(false);

    // when
    // then
    assertThatThrownBy(() -> profileValidationAspect.validateProfile(joinPoint))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_EXISTS_PROFILE);
    verify(profileRepository).existsByUser_Id(userId);
  }

  @Test
  @DisplayName("프로필이 있는 경우 - 성공")
  void successWhenProfileExists() {
    // given
    Long userId = 1L;
    CustomUserDetails userDetails = createCustomUserDetails(userId);
    JoinPoint joinPoint = mock(JoinPoint.class);

    when(joinPoint.getArgs()).thenReturn(new Object[]{userDetails});
    when(profileRepository.existsByUser_Id(userId)).thenReturn(true);

    // when & then
    assertDoesNotThrow(() -> profileValidationAspect.validateProfile(joinPoint));
    verify(profileRepository).existsByUser_Id(userId);
  }

  private static CustomUserDetails createCustomUserDetails(Long userId) {
    return new CustomUserDetails(User.builder()
        .id(userId)
        .email("test@test.com")
        .build());
  }
}