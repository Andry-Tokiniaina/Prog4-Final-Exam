package hei.school.exam.service.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

class JwtServiceTest {

  private static final String SECRET =
      "01234567890123456789012345678901234567890123456789"; // >= 256 bits for HMAC-SHA

  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService(SECRET);
  }

  @Test
  void generateToken_and_extractUsername_round_trip() {
    String token = jwtService.generateToken("jean@example.com");

    assertThat(token).isNotBlank();
    assertThat(jwtService.extractUsername(token)).isEqualTo("jean@example.com");
  }

  @Test
  void isTokenValid_true_for_matching_username() {
    String token = jwtService.generateToken("jean@example.com");
    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn("jean@example.com");

    assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
  }

  @Test
  void isTokenValid_false_for_different_username() {
    String token = jwtService.generateToken("jean@example.com");
    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn("other@example.com");

    assertThat(jwtService.isTokenValid(token, userDetails)).isFalse();
  }

  @Test
  void isTokenValid_false_for_malformed_token() {
    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn("jean@example.com");

    assertThat(jwtService.isTokenValid("not-a-real-token", userDetails)).isFalse();
  }

  @Test
  void isTokenValid_false_for_token_signed_with_different_key() {
    JwtService otherService = new JwtService("98765432109876543210987654321098765432109876543210");
    String token = otherService.generateToken("jean@example.com");

    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn("jean@example.com");

    assertThat(jwtService.isTokenValid(token, userDetails)).isFalse();
  }
}
