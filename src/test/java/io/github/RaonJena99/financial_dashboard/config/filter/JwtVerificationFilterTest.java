package io.github.RaonJena99.financial_dashboard.config.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.github.RaonJena99.financial_dashboard.domain.User;
import io.github.RaonJena99.financial_dashboard.service.CustomUserDetailsService;
import io.github.RaonJena99.financial_dashboard.util.JWTUtil;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtVerificationFilterTest {

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Authorization 헤더가 없으면 체인을 그냥 통과한다")
  void noHeader_passThrough() throws ServletException, IOException {
    JWTUtil jwtUtil = Mockito.mock(JWTUtil.class);
    CustomUserDetailsService uds = Mockito.mock(CustomUserDetailsService.class);
    JwtVerificationFilter filter = new JwtVerificationFilter(jwtUtil, uds);

    var req = new MockHttpServletRequest("GET", "/secured/ping");
    var res = new MockHttpServletResponse();
    var chain = new MockFilterChain();

    filter.doFilter(req, res, chain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  @DisplayName("토큰 검증이 실패하면 체인을 그냥 통과한다")
  void invalidToken_passThrough() throws ServletException, IOException {
    JWTUtil jwtUtil = Mockito.mock(JWTUtil.class);
    CustomUserDetailsService uds = Mockito.mock(CustomUserDetailsService.class);
    JwtVerificationFilter filter = new JwtVerificationFilter(jwtUtil, uds);

    when(jwtUtil.validateToken("bad")).thenReturn(false);

    var req = new MockHttpServletRequest("GET", "/secured/ping");
    req.addHeader("Authorization", "Bearer bad");
    var res = new MockHttpServletResponse();
    var chain = new MockFilterChain();

    filter.doFilter(req, res, chain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  @DisplayName("토큰 검증 성공 시 SecurityContext에 Authentication을 세팅한다")
  void validToken_setsAuthentication() throws ServletException, IOException {
    JWTUtil jwtUtil = Mockito.mock(JWTUtil.class);
    CustomUserDetailsService uds = Mockito.mock(CustomUserDetailsService.class);
    JwtVerificationFilter filter = new JwtVerificationFilter(jwtUtil, uds);

    String token = "good";
    String email = "user@example.com";

    when(jwtUtil.validateToken(token)).thenReturn(true);
    when(jwtUtil.getEmail(token)).thenReturn(email);

    User user = new User();
    set(user, "email", email);
    set(user, "username", email);

    when(uds.loadUserByUsername(email)).thenReturn(user);

    var req = new MockHttpServletRequest("GET", "/secured/ping");
    req.addHeader("Authorization", "Bearer " + token);
    var res = new MockHttpServletResponse();
    var chain = new MockFilterChain();

    filter.doFilter(req, res, chain);

    var auth = SecurityContextHolder.getContext().getAuthentication();
    assertThat(auth).isNotNull();
    assertThat(auth.getPrincipal()).isEqualTo(user);
    assertThat(auth.getAuthorities()).isEqualTo(user.getAuthorities());
  }

  private static void set(Object target, String fieldName, Object value) {
    Class<?> c = target.getClass();
    java.lang.reflect.Field f = null;

    while (c != null) {
      try {
        f = c.getDeclaredField(fieldName);
        break;
      } catch (NoSuchFieldException e) {
        c = c.getSuperclass();
      }
    }
    if (f == null) {
      throw new IllegalArgumentException(
          "Field not found: " + fieldName + " on " + target.getClass().getName());
    }

    f.setAccessible(true);
    try {
      f.set(target, value);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("Failed to set field: " + fieldName, e);
    }
  }
}
