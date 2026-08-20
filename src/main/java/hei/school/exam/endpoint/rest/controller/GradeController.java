package hei.school.exam.endpoint.rest.controller;

import hei.school.exam.dto.*;
import hei.school.exam.entity.Student;
import hei.school.exam.entity.User;
import hei.school.exam.service.event.GradeService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class GradeController {
  private final GradeService gradeService;

  @GetMapping("/students/me/grades")
  @PreAuthorize("hasRole('STUDENT')")
  public List<GradeDto> myGrades(@AuthenticationPrincipal Student principal) {
    return gradeService.findByStudent(principal.getId()).stream().map(DtoMapper::toDto).toList();
  }

  @GetMapping("/students/{studentId}/grades")
  @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
  public List<GradeDto> byStudent(
      @PathVariable UUID studentId, @AuthenticationPrincipal User principal) {
    return gradeService.findByStudentForUser(studentId, principal).stream()
        .map(DtoMapper::toDto)
        .toList();
  }

  @GetMapping("/courses/{courseId}/grades")
  @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
  public List<GradeDto> byCourse(
      @PathVariable UUID courseId, @AuthenticationPrincipal User principal) {
    return gradeService.findByCourseForUser(courseId, principal).stream()
        .map(DtoMapper::toDto)
        .toList();
  }

  @GetMapping("/exams/{examId}/grades")
  @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
  public List<GradeDto> byExam(@PathVariable UUID examId, @AuthenticationPrincipal User principal) {
    return gradeService.findByExamForUser(examId, principal).stream()
        .map(DtoMapper::toDto)
        .toList();
  }

  @PostMapping("/grades")
  @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
  @ResponseStatus(HttpStatus.CREATED)
  public GradeDto create(@RequestBody GradeInput input, @AuthenticationPrincipal User principal) {
    return DtoMapper.toDto(
        gradeService.create(input.studentId(), input.examId(), input.value(), principal));
  }

  @GetMapping("/grades/{gradeId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
  public GradeDto get(@PathVariable UUID gradeId, @AuthenticationPrincipal User principal) {
    return DtoMapper.toDto(gradeService.getForUser(gradeId, principal));
  }

  @PutMapping("/grades/{gradeId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
  public GradeDto update(
      @PathVariable UUID gradeId,
      @RequestBody GradeUpdateInput input,
      @AuthenticationPrincipal User principal) {
    return DtoMapper.toDto(gradeService.update(gradeId, input.value(), input.reason(), principal));
  }

  @GetMapping("/grades/{gradeId}/history")
  @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
  public List<GradeHistoryEntryDto> history(
      @PathVariable UUID gradeId, @AuthenticationPrincipal User principal) {
    return gradeService.findHistoryForUser(gradeId, principal).stream()
        .map(DtoMapper::toDto)
        .toList();
  }
}
