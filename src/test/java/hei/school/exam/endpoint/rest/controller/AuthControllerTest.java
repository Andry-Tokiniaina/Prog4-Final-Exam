package hei.school.exam.endpoint.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hei.school.exam.dto.LoginRequest;
import hei.school.exam.dto.LoginResponse;
import hei.school.exam.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class AuthControllerTest {

  @Test
  void login_returns_ok_with_token() {
    AuthService authService = mock(AuthService.class);
    LoginRequest request = new LoginRequest("jean@example.com", "pwd");
    when(authService.login(request)).thenReturn(new LoginResponse("token123"));

    AuthController controller = new AuthController(authService);

    ResponseEntity<?> response = controller.login(request);

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(((LoginResponse) response.getBody()).token()).isEqualTo("token123");
  }
}
