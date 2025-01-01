package com.momo.config;

import com.momo.auth.security.CustomLogoutFilter;
import com.momo.auth.security.LoginFilter;
import com.momo.config.constants.EndpointConstants;
import com.momo.config.token.repository.RefreshTokenRepository;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final AuthenticationConfiguration authenticationConfiguration;
  private final UserRepository userRepository;
  private final JWTUtil jwtUtil;
  private final RefreshTokenRepository refreshTokenRepository;


  public SecurityConfig(AuthenticationConfiguration authenticationConfiguration,
      UserRepository userRepository,JWTUtil jwtUtil,
      RefreshTokenRepository refreshTokenRepository) {
    this.authenticationConfiguration = authenticationConfiguration;
    this.userRepository = userRepository;
    this.jwtUtil = jwtUtil;
    this.refreshTokenRepository = refreshTokenRepository;

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

    // JWT 기반 인증 필터 추가
    JWTFilter jwtFilter = new JWTFilter(userRepository, jwtUtil);

    // 로그인 필터 추가
    LoginFilter loginFilter = new LoginFilter(authenticationManager(authenticationConfiguration),
        jwtUtil, refreshTokenRepository, userRepository);

    // 로그아웃 필터 추가
    CustomLogoutFilter logoutFilter = new CustomLogoutFilter(jwtUtil, refreshTokenRepository);

    http
        .csrf(csrf -> csrf.disable())
        .formLogin(formLogin -> formLogin.disable())
        .httpBasic(httpBasic -> httpBasic.disable())
        .authorizeRequests(authz -> authz
            .antMatchers(
                EndpointConstants.ROOT,
                EndpointConstants.USERS_API,
                EndpointConstants.TOKEN_REISSUE,
                "/h2-console/**",
                "/ws/**"
            ).permitAll()
            .anyRequest().authenticated()
        )
        .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())) // 프레임 옵션 비활성화
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // JWT 기반으로 Stateless 설정
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class) // JWT 필터 추가
        .addFilterBefore(loginFilter, UsernamePasswordAuthenticationFilter.class) // Login 필터 추가
        .addFilterBefore(logoutFilter, UsernamePasswordAuthenticationFilter.class); // Logout 필터 추가

    return http.build();
  }
}
