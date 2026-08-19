package hei.school.exam.endpoint.rest.controller;

import hei.school.exam.dto.CourseDto;
import hei.school.exam.dto.DtoMapper;
import hei.school.exam.dto.TeacherDto;
import hei.school.exam.dto.TeacherInput;
import hei.school.exam.dto.TeacherUpdateInput;
import hei.school.exam.entity.Teacher;
import hei.school.exam.service.event.TeacherService;
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
public class TeacherController {
  private final TeacherService teacherService;

  @GetMapping("/teachers")
  @PreAuthorize("hasRole('ADMIN')")
  public List<TeacherDto> list() {
    return teacherService.findAll().stream().map(DtoMapper::toDto).toList();
  }

  @PostMapping("/teachers")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.CREATED)
  public TeacherDto create(@Valid @RequestBody TeacherInput input) {
    return DtoMapper.toDto(teacherService.create(input));
  }

  @GetMapping("/teachers/{teacherId}")
  @PreAuthorize("hasRole('ADMIN')")
  public TeacherDto get(@PathVariable UUID teacherId) {
    return DtoMapper.toDto(teacherService.findById(teacherId));
  }

  @PutMapping("/teachers/{teacherId}")
  @PreAuthorize("hasRole('ADMIN')")
  public TeacherDto update(@PathVariable UUID teacherId, @RequestBody TeacherUpdateInput input) {
    return DtoMapper.toDto(teacherService.update(teacherId, input));
  }

  @DeleteMapping("/teachers/{teacherId}")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID teacherId) {
    teacherService.delete(teacherId);
  }

  @GetMapping("/teachers/me/courses")
  @PreAuthorize("hasRole('TEACHER')")
  public List<CourseDto> myCourses(@AuthenticationPrincipal Teacher principal) {
    return teacherService.findCourses(principal.getId()).stream().map(DtoMapper::toDto).toList();
  }
}
