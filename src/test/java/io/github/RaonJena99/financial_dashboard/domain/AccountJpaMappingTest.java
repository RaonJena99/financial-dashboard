package io.github.RaonJena99.financial_dashboard.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.ANY)
@DisplayName("Account 엔티티 JPA 매핑 테스트")
class AccountJpaMappingTest {

  @Autowired private TestEntityManager em;

  @Test
  @DisplayName("User(영속)와 함께 Account가 정상 저장된다")
  void persist_withManagedUser_success() {
    // given: 영속된 사용자
    User user = new User();
    user.setEmail("acc-owner@example.com");
    user.setUsername("owner");
    user.setPassword("pw");
    em.persistAndFlush(user);

    // and: 계좌
    Account account = new Account();
    account.setUser(user);
    account.setAccountNumber("123-456-7890");
    account.setAccountName("내적금");
    account.setBankName("MyBank");

    // when
    Account saved = em.persistAndFlush(account);

    // then
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getUser()).isNotNull();
    assertThat(saved.getUser().getId()).isEqualTo(user.getId());
    assertThat(saved.getAccountNumber()).isEqualTo("123-456-7890");
    assertThat(saved.getBankName()).isEqualTo("MyBank");
  }

  @Test
  @DisplayName("accountNumber가 null이면 NOT NULL 제약으로 저장 실패")
  void persist_withoutAccountNumber_failsByNotNullConstraint() {
    // given
    User user = new User();
    user.setEmail("null-check@example.com");
    user.setUsername("nullcheck");
    user.setPassword("pw");
    em.persistAndFlush(user);

    Account account = new Account();
    account.setUser(user);
    account.setAccountNumber(null); // @Column(nullable = false)
    account.setAccountName("별칭");
    account.setBankName("Bank");

    // when & then
    assertThatThrownBy(() -> em.persistAndFlush(account))
        // DB 제약 위반 시 jpa/h2 조합에서 PersistenceException/DataIntegrityViolationException 등으로 래핑될 수 있음
        .isInstanceOf(PersistenceException.class);
  }

  @Test
  @DisplayName("비영속(Transient) User를 참조한 Account 저장 시 예외 발생")
  void persist_withTransientUser_fails() {
    // given: 아직 영속화하지 않은 User
    User transientUser = new User();
    transientUser.setEmail("transient@example.com");
    transientUser.setUsername("transient");
    transientUser.setPassword("pw");

    Account account = new Account();
    account.setUser(transientUser); // 영속X
    account.setAccountNumber("777-888-9999");

    // when & then
    assertThatThrownBy(() -> em.persistAndFlush(account))
        // Hibernate 6에서는 IllegalStateException/PersistenceException 등으로 래핑될 수 있음
        .isInstanceOfAny(
            IllegalStateException.class, jakarta.persistence.PersistenceException.class)
        // 핵심은 root cause가 TransientObjectException 인지 확인
        .hasRootCauseInstanceOf(org.hibernate.TransientObjectException.class);
  }
}
