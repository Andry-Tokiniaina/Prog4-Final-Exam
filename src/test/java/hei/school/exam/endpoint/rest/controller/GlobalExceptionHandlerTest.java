package hei.school.exam.endpoint.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;

import hei.school.exam.dto.ErrorDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void handleAccessDenied_returns_403() {
    ResponseEntity<ErrorDto> response =
        handler.handleAccessDenied(new AccessDeniedException("nope"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(response.getBody().message()).isEqualTo("nope");
    assertThat(response.getBody().code()).isEqualTo("FORBIDDEN");
  }

  @Test
  void handleResponseStatus_uses_exception_status_and_reason() {
    ResponseStatusException exception =
        new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "invalid credits");

    ResponseEntity<ErrorDto> response = handler.handleResponseStatus(exception);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(response.getBody().message()).isEqualTo("invalid credits");
  }

  @Test
  void handleRuntimeException_returns_404_when_message_contains_not_found() {
    ResponseEntity<ErrorDto> response =
        handler.handleRuntimeException(new RuntimeException("Course not found"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void handleRuntimeException_returns_500_for_other_messages() {
    ResponseEntity<ErrorDto> response =
        handler.handleRuntimeException(new RuntimeException("boom"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void handleRuntimeException_returns_500_when_message_null() {
    ResponseEntity<ErrorDto> response = handler.handleRuntimeException(new RuntimeException());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
