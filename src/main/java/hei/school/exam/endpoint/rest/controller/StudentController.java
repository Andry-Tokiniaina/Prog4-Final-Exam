package hei.school.exam.endpoint.rest.controller;

import hei.school.exam.dto.DtoMapper;
import hei.school.exam.dto.StudentDto;
import hei.school.exam.dto.StudentInput;
import hei.school.exam.dto.StudentUpdateInput;
import hei.school.exam.dto.TranscriptRequestAcceptedDto;
import hei.school.exam.entity.Student;
import hei.school.exam.service.event.StudentService;
import hei.school.exam.service.event.TranscriptService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class StudentController {
  private final StudentService studentService;
  private final TranscriptService transcriptService;

  @GetMapping("/students/me")
  @PreAuthorize("hasRole('STUDENT')")
  public StudentDto me(@AuthenticationPrincipal Student principal) {
    return DtoMapper.toDto(studentService.findById(principal.getId()));
  }

  @PostMapping("/students/me/transcript")
  @PreAuthorize("hasRole('STUDENT')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public TranscriptRequestAcceptedDto requestMyTranscript(
      @AuthenticationPrincipal Student principal) {
    transcriptService.requestTranscriptByEmail(principal.getId());
    return new TranscriptRequestAcceptedDto("The transcript will be sent by email shortly.");
  }

  @GetMapping("/students")
  @PreAuthorize("hasRole('ADMIN')")
  public List<StudentDto> list(
      @RequestParam(required = false) UUID cohortId,
      @RequestParam(required = false) UUID groupId,
      @RequestParam(required = false) UUID trackId) {
    return studentService.findAll(cohortId, groupId, trackId).stream()
        .map(DtoMapper::toDto)
        .toList();
  }

  @PostMapping("/students")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.CREATED)
  public StudentDto create(@Valid @RequestBody StudentInput input) {
    return DtoMapper.toDto(studentService.create(input));
  }

  @GetMapping("/students/{studentId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
  public StudentDto get(
      @PathVariable UUID studentId,
      @AuthenticationPrincipal hei.school.exam.entity.User principal) {
    return DtoMapper.toDto(studentService.getForUser(studentId, principal));
  }

  @PutMapping("/students/{studentId}")
  @PreAuthorize("hasRole('ADMIN')")
  public StudentDto update(@PathVariable UUID studentId, @RequestBody StudentUpdateInput input) {
    return DtoMapper.toDto(studentService.update(studentId, input));
  }

  @DeleteMapping("/students/{studentId}")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID studentId) {
    studentService.delete(studentId);
  }

  @PutMapping("/students/{studentId}/group")
  @PreAuthorize("hasRole('ADMIN')")
  public StudentDto changeGroup(@PathVariable UUID studentId, @RequestBody GroupIdBody body) {
    return DtoMapper.toDto(studentService.changeGroup(studentId, body.groupId()));
  }

  @PostMapping("/students/{studentId}/transcript")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public TranscriptRequestAcceptedDto requestTranscript(@PathVariable UUID studentId) {
    transcriptService.requestTranscriptByEmail(studentId);
    return new TranscriptRequestAcceptedDto("The transcript will be sent by email shortly.");
  }

  public record GroupIdBody(UUID groupId) {}

  @GetMapping("/students/{studentId}/diploma")
  @PreAuthorize("hasRole('ADMIN')")
  public boolean diploma(@PathVariable UUID studentId) {
    return studentService.hasDiploma(studentId);
  }
}
