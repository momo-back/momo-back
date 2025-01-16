package com.momo.config;

import com.momo.config.interceptor.ProfileValidationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final ProfileValidationInterceptor profileValidationInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(profileValidationInterceptor)
        .addPathPatterns("/api/v1/meetings")
        .addPathPatterns("/api/v1/notifications/**");
  }
}
