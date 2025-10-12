package io.github.RaonJena99.financial_dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import io.github.RaonJena99.financial_dashboard.domain.User;
import io.github.RaonJena99.financial_dashboard.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService 단위 테스트")
class CustomUserDetailsServiceTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private CustomUserDetailsService service;

  @Test
  @DisplayName("이메일로 사용자를 찾으면 UserDetails를 반환한다")
  void loadUserByUsername_found_returnsUserDetails() {
    // given
    String email = "user@example.com";
    User user = new User();
    user.setId(1L);
    user.setEmail(email);
    user.setUsername("display-name"); // 필드명 그대로 (getUsername()은 email 반환)
    user.setPassword("encoded");

    given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

    // when
    UserDetails result = service.loadUserByUsername(email);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getUsername()).isEqualTo(email); // UserDetails 규약: email 반환
    assertThat(result.getPassword()).isEqualTo("encoded");
    verify(userRepository).findByEmail(email);
  }

  @Test
  @DisplayName("이메일로 사용자를 찾지 못하면 UsernameNotFoundException을 던진다")
  void loadUserByUsername_notFound_throws() {
    // given
    String missing = "missing@example.com";
    given(userRepository.findByEmail(missing)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> service.loadUserByUsername(missing))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining(missing);

    verify(userRepository).findByEmail(missing);
  }
}
