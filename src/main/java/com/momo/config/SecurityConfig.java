package com.momo.config;

import com.momo.auth.security.CustomLogoutFilter;
import com.momo.auth.security.LoginFilter;
import com.momo.config.constants.EndpointConstants;
import com.momo.config.token.repository.RefreshTokenRepository;
import com.momo.user.service.CustomUserDetailsService;
import com.momo.user.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final AuthenticationConfiguration authenticationConfiguration;
  private final CustomUserDetailsService customUserDetailsService;
  private final UserRepository userRepository;
  private final JWTUtil jwtUtil;
  private final RefreshTokenRepository refreshTokenRepository;

  public SecurityConfig(
      AuthenticationConfiguration authenticationConfiguration,
      CustomUserDetailsService customUserDetailsService,
      UserRepository userRepository,
      JWTUtil jwtUtil,
      RefreshTokenRepository refreshTokenRepository) {
    this.authenticationConfiguration = authenticationConfiguration;
    this.customUserDetailsService = customUserDetailsService;
    this.userRepository = userRepository;
    this.jwtUtil = jwtUtil;
    this.refreshTokenRepository = refreshTokenRepository;
  }

  @Bean
  public BCryptPasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(customUserDetailsService);
    authProvider.setPasswordEncoder(bCryptPasswordEncoder());
    return authProvider;
  }

  // RestTemplate 빈 등록
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    JWTFilter jwtFilter = new JWTFilter(userRepository, jwtUtil);
    LoginFilter loginFilter = new LoginFilter(authenticationManager(), jwtUtil, refreshTokenRepository, userRepository);
    CustomLogoutFilter logoutFilter = new CustomLogoutFilter(jwtUtil, refreshTokenRepository);

    http
        .csrf(csrf -> csrf.disable())
        .formLogin(formLogin -> formLogin.disable())
        .httpBasic(httpBasic -> httpBasic.disable())
        .authorizeRequests(authz -> authz
            .antMatchers(
                EndpointConstants.ROOT,
                EndpointConstants.USERS_API,
                EndpointConstants.KAKAO_API,
                EndpointConstants.TOKEN_REISSUE,
                EndpointConstants.KAKAO_LOGIN,
                EndpointConstants.KAKAO_LOGOUT
            ).permitAll()
            .anyRequest().authenticated()
        )
        .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(loginFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(logoutFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  // 기존 AuthenticationManager 메서드 사용
  @Bean
  public AuthenticationManager authenticationManager() throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }
}

