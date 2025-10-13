package io.github.RaonJena99.financial_dashboard.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.PersistenceException;
import java.time.LocalDateTime;
import org.hibernate.TransientObjectException;
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
@DisplayName("Transaction 엔티티 JPA 매핑 테스트")
class TransactionJpaMappingTest {

  @Autowired private TestEntityManager em;

  @Test
  @DisplayName("영속된 Account와 함께 Transaction이 정상 저장된다")
  void persist_withManagedAccount_success() {
    // given: 사용자 및 계좌를 영속화
    User user = new User();
    user.setEmail("t-owner@example.com");
    user.setUsername("towner");
    user.setPassword("pw");
    em.persist(user);

    Account account = new Account();
    account.setUser(user);
    account.setAccountNumber("111-222-3333");
    account.setAccountName("체크카드");
    account.setBankName("MyBank");
    em.persistAndFlush(account);

    // when: 거래 저장
    Transaction tx = new Transaction();
    tx.setAccount(account);
    tx.setAmount(25_000L);
    tx.setMerchantName("커피숍");
    tx.setCategory("식비");
    tx.setType("EXPENSE");
    tx.setTransactionDate(LocalDateTime.now());

    Transaction saved = em.persistAndFlush(tx);

    // then
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getAccount()).isNotNull();
    assertThat(saved.getAccount().getId()).isEqualTo(account.getId());
    assertThat(saved.getAmount()).isEqualTo(25_000L);
    assertThat(saved.getMerchantName()).isEqualTo("커피숍");
    assertThat(saved.getType()).isEqualTo("EXPENSE");
  }

  @Test
  @DisplayName("비영속 Account를 참조한 Transaction 저장 시 예외 발생")
  void persist_withTransientAccount_fails() {
    // given: 아직 영속화하지 않은 계정/계좌
    User transientUser = new User();
    transientUser.setEmail("transient@example.com");
    transientUser.setUsername("transient");
    transientUser.setPassword("pw");

    Account transientAccount = new Account();
    transientAccount.setUser(transientUser); // 둘 다 영속 아님
    transientAccount.setAccountNumber("999-888-7777");

    Transaction tx = new Transaction();
    tx.setAccount(transientAccount);
    tx.setAmount(1000L);
    tx.setTransactionDate(LocalDateTime.now());

    // when & then
    assertThatThrownBy(() -> em.persistAndFlush(tx))
        // Hibernate 6에서는 IllegalStateException/PersistenceException 등으로 감싸질 수 있음
        .isInstanceOfAny(IllegalStateException.class, PersistenceException.class)
        .hasRootCauseInstanceOf(TransientObjectException.class);
  }

  @Test
  @DisplayName("Account 없이(null) Transaction 저장이 가능하다(스키마상 FK nullable)")
  void persist_withoutAccount_nullableFk_allowsSave() {
    // given
    Transaction tx = new Transaction();
    tx.setAccount(null); // FK nullable
    tx.setAmount(5000L);
    tx.setMerchantName("편의점");
    tx.setCategory("식비");
    tx.setType("EXPENSE");
    tx.setTransactionDate(LocalDateTime.now());

    // when
    Transaction saved = em.persistAndFlush(tx);

    // then
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getAccount()).isNull();
    assertThat(saved.getAmount()).isEqualTo(5000L);
  }
}
