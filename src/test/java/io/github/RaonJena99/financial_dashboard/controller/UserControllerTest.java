package io.github.RaonJena99.financial_dashboard.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.RaonJena99.financial_dashboard.domain.User;
import io.github.RaonJena99.financial_dashboard.service.UserService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // 시큐리티 필터 비활성화
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("UserController 통합 테스트")
class UserControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private UserService userService;

  @Test
  @DisplayName("POST /api/users/register - 사용자 등록 성공")
  void registerUser_success() throws Exception {
    // given (요청)
    User requestUser = new User();
    setField(requestUser, "username", "raon"); // 실제 필드와 맞추세요
    setField(requestUser, "email", "raon@example.com");
    setField(requestUser, "password", "plain-password");

    // given (서비스 반환)
    User savedUser = new User();
    setField(savedUser, "id", 1L);
    setField(savedUser, "email", "raon@example.com");
    setField(savedUser, "username", "raon@example.com");

    when(userService.registerUser(any(User.class))).thenReturn(savedUser);

    // when & then
    mockMvc
        .perform(
            post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestUser)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.email").value("raon@example.com"))
        // ↓ UserDetails#getUsername()이 email을 반환한다면 다음 검증을 유지
        .andExpect(jsonPath("$.username").value("raon@example.com"));
  }

  @Test
  @DisplayName("POST /api/users/register - 서비스 예외 시 예외가 전달된다(전역 예외 처리기 없음 가정)")
  void registerUser_serviceThrows_throwsServletException() throws Exception {
    // given
    User requestUser = new User();
    setField(requestUser, "email", "dup@example.com");

    when(userService.registerUser(any(User.class)))
        .thenThrow(new RuntimeException("duplicate user"));

    // when & then: 상태코드 대신 예외 발생을 기대
    ServletException ex =
        assertThrows(
            ServletException.class,
            () ->
                mockMvc
                    .perform(
                        post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestUser)))
                    .andReturn());

    // 원인까지 확인
    assertThat(ex.getCause()).isInstanceOf(RuntimeException.class);
    assertThat(ex.getCause()).hasMessage("duplicate user");
  }

  // -------- 테스트 유틸 (필드 세팅) --------
  private static void setField(Object target, String fieldName, Object value) {
    try {
      java.lang.reflect.Field f = null;
      Class<?> c = target.getClass();
      while (c != null) {
        try {
          f = c.getDeclaredField(fieldName);
          break;
        } catch (NoSuchFieldException ignored) {
          c = c.getSuperclass();
        }
      }
      if (f == null) return;
      f.setAccessible(true);
      f.set(target, value);
    } catch (IllegalAccessException ignored) {
    }
  }
}
