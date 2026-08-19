package hei.school.exam.endpoint.rest.controller;

import hei.school.exam.dto.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorDto> handleAccessDenied(AccessDeniedException e) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(new ErrorDto(e.getMessage(), "FORBIDDEN"));
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ErrorDto> handleResponseStatus(ResponseStatusException e) {
    return ResponseEntity.status(e.getStatusCode())
        .body(new ErrorDto(e.getReason(), e.getStatusCode().toString()));
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorDto> handleRuntimeException(RuntimeException e) {
    HttpStatus status =
        e.getMessage() != null && e.getMessage().toLowerCase().contains("not found")
            ? HttpStatus.NOT_FOUND
            : HttpStatus.INTERNAL_SERVER_ERROR;
    return ResponseEntity.status(status).body(new ErrorDto(e.getMessage(), status.toString()));
  }
}
