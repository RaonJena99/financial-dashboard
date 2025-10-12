package io.github.RaonJena99.financial_dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.RaonJena99.financial_dashboard.domain.User;
import io.github.RaonJena99.financial_dashboard.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl 단위 테스트")
class UserServiceImplTest {

  @Mock private UserRepository userRepository;
  @Mock private BCryptPasswordEncoder bCryptPasswordEncoder;

  @InjectMocks private UserServiceImpl userService;

  @Test
  @DisplayName("registerUser: 비밀번호를 BCrypt로 인코딩하여 저장한다")
  void registerUser_encodesPasswordAndSaves() {
    // given
    User raw = new User();
    raw.setUsername("raon");
    raw.setEmail("raon@example.com");
    raw.setPassword("plain"); // 원문 비밀번호

    String encoded = "$2a$10$encodedPwHash"; // 더미 인코딩 값
    given(bCryptPasswordEncoder.encode("plain")).willReturn(encoded);

    // repository.save가 반환할 엔티티(보통 ID가 채워진 상태라고 가정)
    User saved = new User();
    saved.setId(1L);
    saved.setUsername("raon");
    saved.setEmail("raon@example.com");
    saved.setPassword(encoded);
    given(userRepository.save(any(User.class))).willReturn(saved);

    // when
    User result = userService.registerUser(raw);

    // then
    // 1) 인코딩이 호출되었고, 저장된 엔티티에 인코딩된 비밀번호가 들어갔는지 캡쳐로 확인
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(bCryptPasswordEncoder, times(1)).encode("plain");
    verify(userRepository, times(1)).save(captor.capture());

    User toSave = captor.getValue();
    assertThat(toSave.getEmail()).isEqualTo("raon@example.com");
    assertThat(toSave.getUsername()).isEqualTo("raon@example.com");
    assertThat(toSave.getPassword()).isEqualTo(encoded); // 저장 전에 이미 인코딩 완료

    // 2) 서비스 리턴 값도 저장 결과를 그대로 돌려받음
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getEmail()).isEqualTo("raon@example.com");
    assertThat(result.getPassword()).isEqualTo(encoded);
  }
}
