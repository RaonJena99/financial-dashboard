package io.github.RaonJena99.financial_dashboard.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users") // DB 테이블 이름
@Getter
@Setter
@NoArgsConstructor // 파라미터가 없는 기본 생성자 생성
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String username;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password; // 암호화하여 저장
}
// hook test
// hook test
// hook test
