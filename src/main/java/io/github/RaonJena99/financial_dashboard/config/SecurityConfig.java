package io.github.RaonJena99.financial_dashboard.config;

import io.github.RaonJena99.financial_dashboard.config.filter.JwtAuthenticationFilter;
import io.github.RaonJena99.financial_dashboard.config.filter.JwtVerificationFilter;
import io.github.RaonJena99.financial_dashboard.service.CustomUserDetailsService;
import io.github.RaonJena99.financial_dashboard.util.JWTUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  private final JWTUtil jwtUtil;
  private final CustomUserDetailsService customUserDetailsService;

  public SecurityConfig(JWTUtil jwtUtil, CustomUserDetailsService customUserDetailsService) {
    this.jwtUtil = jwtUtil;
    this.customUserDetailsService = customUserDetailsService;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
      throws Exception {
    return configuration.getAuthenticationManager();
  }

  @Bean
  public BCryptPasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authManager)
      throws Exception {
    // Form 로그인 방식 비활성화
    http.formLogin(AbstractHttpConfigurer::disable);

    // HTTP Basic 인증 방식 비활성화
    http.httpBasic(AbstractHttpConfigurer::disable);

    // 경로별 인가 작업
    http.authorizeHttpRequests(
        (auth) ->
            auth.requestMatchers(
                    "/",
                    "/api/users/register",
                    "/api/users/login",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/error")
                .permitAll() // 회원가입 및 로그인 경로는 모두 허용
                .anyRequest()
                .authenticated() // 그 외 모든 요청은 인증 필요
        );
    // 커스텀 필터 추가
    http.addFilterAt(
        new JwtAuthenticationFilter(authManager, jwtUtil),
        UsernamePasswordAuthenticationFilter.class);
    http.addFilterBefore(
        new JwtVerificationFilter(jwtUtil, customUserDetailsService),
        JwtAuthenticationFilter.class);

    // 세션 설정: 상태 비저장(Stateless)으로 설정
    http.sessionManagement(
        (session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    // CSRF 보호 비활성화 (JWT으로 Stateless 상태를 유지 할 경우[세션에 안 저장] )
    http.csrf(AbstractHttpConfigurer::disable);

    return http.build();
  }
}
