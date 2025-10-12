package io.github.RaonJena99.financial_dashboard.config;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import io.github.RaonJena99.financial_dashboard.domain.User;
import io.github.RaonJena99.financial_dashboard.service.CustomUserDetailsService;
import io.github.RaonJena99.financial_dashboard.service.UserService;
import io.github.RaonJena99.financial_dashboard.util.JWTUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(SecurityIntegrationTest.SecuredPingController.class)
@DisplayName("SecurityConfig + JWT 필터 통합 테스트")
class SecurityIntegrationTest {

  @Autowired private MockMvc mvc;

  @MockitoBean private UserService userService;
  @MockitoBean private JWTUtil jwtUtil;
  @MockitoBean private CustomUserDetailsService customUserDetailsService;

  @RestController
  static class SecuredPingController {
    @GetMapping("/secured/ping")
    public String ping() {
      return "pong";
    }
  }

  @Test
  @DisplayName("permitAll 경로(/api/users/register)는 인증 없이 200")
  void permitAll_register_isOk() throws Exception {
    when(userService.registerUser(org.mockito.ArgumentMatchers.any()))
        .thenAnswer(inv -> inv.getArgument(0));

    mvc.perform(
            post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"e@x.com\",\"password\":\"pw\",\"username\":\"u\"}"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("보호 경로는 토큰 없으면 403(EntryPoint 미설정 시 기본 동작)")
  void protected_withoutToken_is403() throws Exception {
    mvc.perform(get("/secured/ping")).andExpect(status().isForbidden()); // ⬅️ 401 → 403 으로 수정
  }

  @Test
  @DisplayName("보호 경로는 유효 토큰이면 200")
  void protected_withValidToken_is200() throws Exception {
    String token = "valid.jwt.token";
    String email = "user@example.com";

    when(jwtUtil.validateToken(token)).thenReturn(true);
    when(jwtUtil.getEmail(token)).thenReturn(email);

    User user = new User();
    set(user, "email", email);
    set(user, "username", email);

    when(customUserDetailsService.loadUserByUsername(anyString())).thenReturn(user);

    mvc.perform(get("/secured/ping").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(content().string("pong"));
  }

  private static void set(Object target, String fieldName, Object value) {
    Class<?> c = target.getClass();
    java.lang.reflect.Field f = null;

    // 상속 계층에서 필드 탐색 (필드 없으면 NoSuchFieldException만 처리)
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
