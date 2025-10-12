package io.github.RaonJena99.financial_dashboard.config.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.RaonJena99.financial_dashboard.domain.User;
import io.github.RaonJena99.financial_dashboard.util.JWTUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

class JwtAuthenticationFilterTest {

  private final ObjectMapper om = new ObjectMapper();

  // 로그인 엔드포인트를 제공할 더미 컨트롤러(필터 뒤로 체인 진행용)
  @RestController
  static class LoginEchoController {
    @PostMapping("/api/users/login")
    public String ok() {
      return "ok";
    }
  }

  @Test
  @DisplayName("성공 인증 시 Authorization: Bearer <token> 헤더를 추가한다")
  void successfulAuthentication_addsHeader() throws Exception {
    // given
    AuthenticationManager authManager = Mockito.mock(AuthenticationManager.class);
    JWTUtil jwtUtil = Mockito.mock(JWTUtil.class);

    User principal = new User();
    set(principal, "email", "e@x.com");
    set(principal, "username", "e@x.com");

    Authentication authResult =
        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

    when(authManager.authenticate(any(Authentication.class))).thenReturn(authResult);
    when(jwtUtil.generateToken("e@x.com")).thenReturn("jwt-token");

    JwtAuthenticationFilter filter = new JwtAuthenticationFilter(authManager, jwtUtil);

    MockMvc mvc =
        MockMvcBuilders.standaloneSetup(new LoginEchoController()).addFilter(filter).build();

    // when & then
    mvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
                    "/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new LoginReq("e@x.com", "pw"))))
        .andExpect(header().string("Authorization", org.hamcrest.Matchers.startsWith("Bearer ")))
        .andExpect(header().string("Authorization", "Bearer jwt-token"));
  }

  @Test
  @DisplayName("인증 실패 시 401과 에러 메시지를 반환한다")
  void unsuccessfulAuthentication_returns401() throws Exception {
    // given
    AuthenticationManager authManager = Mockito.mock(AuthenticationManager.class);
    JWTUtil jwtUtil = Mockito.mock(JWTUtil.class);

    when(authManager.authenticate(any(Authentication.class)))
        .thenThrow(new BadCredentialsException("bad credentials"));

    JwtAuthenticationFilter filter = new JwtAuthenticationFilter(authManager, jwtUtil);

    MockMvc mvc =
        MockMvcBuilders.standaloneSetup(new LoginEchoController()).addFilter(filter).build();

    // when & then
    mvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
                    "/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new LoginReq("e@x.com", "bad"))))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("Authentication Failed")));
  }

  // ---- 테스트용 DTO / 유틸 ----
  record LoginReq(String email, String password) {}

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
