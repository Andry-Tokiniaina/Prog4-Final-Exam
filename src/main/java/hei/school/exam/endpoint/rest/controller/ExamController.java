package hei.school.exam.endpoint.rest.controller;

import hei.school.exam.dto.DtoMapper;
import hei.school.exam.dto.ExamDto;
import hei.school.exam.dto.ExamInput;
import hei.school.exam.entity.User;
import hei.school.exam.service.event.ExamService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ExamController {
  private final ExamService examService;

  @GetMapping("/exams/{examId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
  public ExamDto get(@PathVariable UUID examId, @AuthenticationPrincipal User principal) {
    return DtoMapper.toDto(examService.getForUser(examId, principal));
  }

  @PutMapping("/exams/{examId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
  public ExamDto update(
      @PathVariable UUID examId,
      @RequestBody ExamInput input,
      @AuthenticationPrincipal User principal) {
    return DtoMapper.toDto(examService.update(examId, input, principal));
  }

  @DeleteMapping("/exams/{examId}")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID examId) {
    examService.delete(examId);
  }
}
