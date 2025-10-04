package io.github.RaonJena99.financial_dashboard.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "accounts") // DB 테이블 이름
@Getter
@Setter
@NoArgsConstructor // 파라미터가 없는 기본 생성자 생성
public class Account {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne // User(1) : Account(N) 관계
  @JoinColumn(name = "user_id") // 외래키
  private User user;

  @Column(nullable = false)
  private String accountNumber;

  private String accountName; // 계좌 별칭

  private String bankName; // 금융기관명
}
