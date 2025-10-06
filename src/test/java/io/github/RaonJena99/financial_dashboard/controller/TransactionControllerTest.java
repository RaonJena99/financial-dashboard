package io.github.RaonJena99.financial_dashboard.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.RaonJena99.financial_dashboard.domain.Transaction;
import io.github.RaonJena99.financial_dashboard.service.TransactionService;
import java.time.LocalDateTime;
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

@SpringBootTest // 스프링 부트 통합 테스트 환경 로드
@AutoConfigureMockMvc // MockMvc를 자동으로 설정하여 HTTP 요청 시뮬레이션
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("TransactionController 통합 테스트")
class TransactionControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper; // 객체를 JSON 문자열로 변환

  @MockitoBean // 실제 서비스 대신 가짜 TransactionService Bean을 주입
  private TransactionService transactionService;

  @Test
  @DisplayName("POST /api/transactions 요청 시 거래 생성에 성공한다")
  void createTransaction_success() throws Exception {
    // given
    Transaction transaction = new Transaction();
    transaction.setMerchantName("새로운 상점");
    transaction.setAmount(10000L);
    transaction.setTransactionDate(LocalDateTime.now());

    when(transactionService.createTransaction(any(Transaction.class))).thenReturn(transaction);

    // when & then
    mockMvc
        .perform(
            post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transaction)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.merchantName").value("새로운 상점"));
  }
}
