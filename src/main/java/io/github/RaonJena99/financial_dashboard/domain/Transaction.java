package io.github.RaonJena99.financial_dashboard.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions") // DB 테이블 이름
@Getter
@Setter
@NoArgsConstructor // 파라미터가 없는 기본 생성자 생성
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne // Account(1) : Transaction(N) 관계
    @JoinColumn(name = "account_id") // 외래키
    private Account account;

    private LocalDateTime transactionDate; // 거래 일시

    private Long amount; // 거래 금액

    private String merchantName; // 가맹점명

    private String type; // 입출금 구분 (예: "INCOME", "EXPENSE")

}
