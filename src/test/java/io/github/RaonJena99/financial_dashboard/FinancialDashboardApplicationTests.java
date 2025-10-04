package io.github.RaonJena99.financial_dashboard;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // 테스트에서는 H2 설정 사용
class FinancialDashboardApplicationTests {

  @Test
  void contextLoads() {}
}
