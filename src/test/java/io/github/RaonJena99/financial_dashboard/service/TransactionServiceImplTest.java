package io.github.RaonJena99.financial_dashboard.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.RaonJena99.financial_dashboard.domain.Transaction;
import io.github.RaonJena99.financial_dashboard.repository.TransactionRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class) // Mockito 확장 기능 사용
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("TransactionServiceImpl 단위 테스트")
class TransactionServiceImplTest {

  @Mock // 가짜 TransactionRepository 객체 생성
  private TransactionRepository transactionRepository;

  @InjectMocks // @Mock으로 생성된 객체를 주입받는 TransactionServiceImpl 객체 생성
  private TransactionServiceImpl transactionService;

  @Test
  @DisplayName("getTransactionById() 호출 시 거래를 정상적으로 반환한다")
  void getTransactionById_success() {
    // given: 테스트 준비
    Transaction transaction = new Transaction();
    transaction.setId(1L);
    transaction.setMerchantName("테스트 상점");

    // when: transactionRepository.findById(1L)이 호출되면, 위에서 만든 transaction 객체를 반환하도록 설정
    when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

    // then: 실제 테스트 실행 및 결과 검증
    Optional<Transaction> foundTransaction = transactionService.getTransactionById(1L);

    assertTrue(foundTransaction.isPresent());
    assertEquals("테스트 상점", foundTransaction.get().getMerchantName());

    // findById 메서드가 1번 호출되었는지 검증
    verify(transactionRepository, times(1)).findById(1L);
  }
}
