package io.github.RaonJena99.financial_dashboard.util;

import static org.assertj.core.api.Assertions.*;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("JWTUtil 단위 테스트")
class JWTUtilTest {

  // HS256 최소 256bit(=32 bytes) 이상 권장 → 64자 정도의 시크릿 사용
  private static final String SECRET_A = "this-is-a-very-long-test-secret-key-32bytes-min-aaaaaaaa";
  private static final String SECRET_B =
      "another-different-long-secret-key-for-negative-tests-bbbbb";

  @Test
  @DisplayName("generateToken → validateToken/ getEmail 이 정상 동작한다")
  void generate_and_validate_and_getEmail_success() {
    JWTUtil util = new JWTUtil(SECRET_A);
    String email = "user@example.com";

    String token = util.generateToken(email);

    assertThat(token).isNotBlank();
    assertThat(util.validateToken(token)).isTrue();
    assertThat(util.getEmail(token)).isEqualTo(email);
  }

  @Test
  @DisplayName("서명이 다른 키로 검증하면 validateToken=false, getEmail은 예외")
  void validate_with_different_secret_fails() {
    JWTUtil utilA = new JWTUtil(SECRET_A);
    JWTUtil utilB = new JWTUtil(SECRET_B);

    String token = utilA.generateToken("user@example.com");

    // 다른 키로는 서명 검증 실패
    assertThat(utilB.validateToken(token)).isFalse();

    // getEmail은 JwtException(서명 오류 등) 발생
    assertThatThrownBy(() -> utilB.getEmail(token)).isInstanceOf(JwtException.class);
  }

  @Test
  @DisplayName("말폼(malformed) 토큰은 validateToken=false")
  void malformed_token_returns_false() {
    JWTUtil util = new JWTUtil(SECRET_A);
    assertThat(util.validateToken("not-a-jwt")).isFalse();
  }

  @Test
  @DisplayName("만료된 토큰은 validateToken=false, getEmail 호출 시 ExpiredJwtException")
  void expired_token_returns_false_and_throws_on_getEmail() {
    // 동일한 시크릿으로 직접 만료 토큰 생성 (유틸이 10시간 고정이라 테스트에서 만료 토큰을 만들어 사용)
    SecretKey key = Keys.hmacShaKeyFor(SECRET_A.getBytes(StandardCharsets.UTF_8));
    String email = "expired@example.com";
    String expiredToken =
        Jwts.builder()
            .setSubject(email)
            .setIssuedAt(new Date(System.currentTimeMillis() - 60_000))
            .setExpiration(new Date(System.currentTimeMillis() - 1_000)) // 이미 만료
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();

    JWTUtil util = new JWTUtil(SECRET_A);

    assertThat(util.validateToken(expiredToken)).isFalse();
    assertThatThrownBy(() -> util.getEmail(expiredToken)).isInstanceOf(ExpiredJwtException.class);
  }
}
