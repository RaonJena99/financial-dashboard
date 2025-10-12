package io.github.RaonJena99.financial_dashboard.domain;

import static org.assertj.core.api.Assertions.*;

import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.ANY)
@DisplayName("User 엔티티 매핑 및 UserDetails 구현 테스트")
class UserEntityTest {

  @Autowired private TestEntityManager em;

  @Test
  @DisplayName("정상 값으로 저장/조회된다")
  void persist_and_find_success() {
    User u = new User();
    u.setUsername("raon");
    u.setEmail("raon@example.com");
    u.setPassword("encoded");

    User saved = em.persistAndFlush(u);
    em.clear();

    User found = em.find(User.class, saved.getId());
    assertThat(found).isNotNull();
    assertThat(found.getId()).isNotNull();
    assertThat(found.getEmail()).isEqualTo("raon@example.com");
    assertThat(found.getPassword()).isEqualTo("encoded");
    assertThat(found.getUsername()) // UserDetails#getUsername()
        .isEqualTo("raon@example.com"); // 이메일을 사용자명으로 사용
  }

  @Test
  @DisplayName("email 유니크 제약 위반 시 예외가 발생한다")
  void duplicate_email_throws() {
    User a = new User();
    a.setUsername("a");
    a.setEmail("dup@example.com");
    a.setPassword("pw");
    em.persistAndFlush(a);

    User b = new User();
    b.setUsername("b");
    b.setEmail("dup@example.com"); // 중복
    b.setPassword("pw");

    assertThatThrownBy(() -> em.persistAndFlush(b))
        .isInstanceOfAny(DataIntegrityViolationException.class, PersistenceException.class);
  }

  @Test
  @DisplayName("username이 null이면 NOT NULL 제약으로 저장 실패")
  void null_username_throws() {
    User u = new User();
    u.setUsername(null); // @Column(nullable = false)
    u.setEmail("x@example.com");
    u.setPassword("pw");

    assertThatThrownBy(() -> em.persistAndFlush(u))
        .isInstanceOfAny(DataIntegrityViolationException.class, PersistenceException.class);
  }

  @Test
  @DisplayName("password가 null이면 NOT NULL 제약으로 저장 실패")
  void null_password_throws() {
    User u = new User();
    u.setUsername("u");
    u.setEmail("y@example.com");
    u.setPassword(null); // @Column(nullable = false)

    assertThatThrownBy(() -> em.persistAndFlush(u))
        .isInstanceOfAny(DataIntegrityViolationException.class, PersistenceException.class);
  }

  @Test
  @DisplayName("UserDetails 규약: 권한은 비어 있고 플래그는 모두 true")
  void user_details_contract() {
    User u = new User();
    u.setUsername("raon");
    u.setEmail("raon@example.com");
    u.setPassword("pw");

    assertThat(u.getAuthorities()).isEmpty();
    assertThat(u.isAccountNonExpired()).isTrue();
    assertThat(u.isAccountNonLocked()).isTrue();
    assertThat(u.isCredentialsNonExpired()).isTrue();
    assertThat(u.isEnabled()).isTrue();
    assertThat(u.getUsername()).isEqualTo("raon@example.com"); // email 반환
  }
}
