package com.momo.config;

import com.momo.user.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final AuthenticationConfiguration authenticationConfiguration;
  private final UserRepository userRepository;

  public SecurityConfig(AuthenticationConfiguration authenticationConfiguration, UserRepository userRepository) {
    this.authenticationConfiguration = authenticationConfiguration;
    this.userRepository = userRepository;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
    return configuration.getAuthenticationManager();
  }

  @Bean
  public BCryptPasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf((auth) -> auth.disable());
    http
        .formLogin((auth) -> auth.disable()); //
    http
        .httpBasic((auth) -> auth.disable());
    http
        .authorizeRequests()
        .antMatchers(
            "/",
            "/users/signup",
            "/users/verify/**"
        ).permitAll()
        .anyRequest().authenticated();
    http
        .headers(headers -> headers.frameOptions(frame -> frame.disable())); // 프레임 옵션 비활성화
    http
        .sessionManagement((session) -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    return http.build();
  }
}
