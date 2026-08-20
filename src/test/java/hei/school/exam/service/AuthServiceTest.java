package hei.school.exam.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hei.school.exam.dto.LoginRequest;
import hei.school.exam.dto.LoginResponse;
import hei.school.exam.service.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

class AuthServiceTest {

  private AuthenticationManager authenticationManager;
  private JwtService jwtService;
  private AuthService authService;

  @BeforeEach
  void setUp() {
    authenticationManager = mock(AuthenticationManager.class);
    jwtService = mock(JwtService.class);
    authService = new AuthService(authenticationManager, jwtService);
  }

  @Test
  void login_authenticates_and_returns_token() {
    Authentication authentication = mock(Authentication.class);
    when(authentication.getName()).thenReturn("jean@example.com");
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);
    when(jwtService.generateToken("jean@example.com")).thenReturn("token123");

    LoginResponse response = authService.login(new LoginRequest("jean@example.com", "pwd"));

    assertThat(response.token()).isEqualTo("token123");
  }
}
