package io.github.RaonJena99.financial_dashboard.domain;

import jakarta.persistence.*;
import java.util.Collection;
import java.util.Collections;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "users") // DB 테이블 이름
@Getter
@Setter
@NoArgsConstructor // 파라미터가 없는 기본 생성자 생성
public class User implements UserDetails {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String username;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password; // 암호화하여 저장

  // UserDetails 인터페이스 메서드 구현
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    // 추후 다양한 Role을 만들어 확장 가능
    return Collections.emptyList();
  }

  @Override
  public String getUsername() {
    return this.email; // 사용자 식별자로 email 사용
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
// hook test
// hook test
// hook test
