package hei.school.exam.endpoint.rest.controller;

import hei.school.exam.dto.CourseDto;
import hei.school.exam.dto.DtoMapper;
import hei.school.exam.dto.SemesterDto;
import hei.school.exam.dto.SemesterInput;
import hei.school.exam.service.event.SemesterService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SemesterController {

  private final SemesterService semesterService;

  @GetMapping("/semesters")
  @PreAuthorize("hasRole('ADMIN')")
  public List<SemesterDto> list() {
    return semesterService.findAll().stream().map(DtoMapper::toDto).toList();
  }

  @PostMapping("/semesters")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.CREATED)
  public SemesterDto create(@RequestBody SemesterInput input) {
    return DtoMapper.toDto(semesterService.create(input.number()));
  }

  @GetMapping("/semesters/{semesterId}")
  @PreAuthorize("hasRole('ADMIN')")
  public SemesterDto get(@PathVariable UUID semesterId) {
    return DtoMapper.toDto(semesterService.findById(semesterId));
  }

  @DeleteMapping("/semesters/{semesterId}")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID semesterId) {
    semesterService.delete(semesterId);
  }

  @GetMapping("/semesters/{semesterId}/courses")
  @PreAuthorize("hasRole('ADMIN')")
  public List<CourseDto> courses(@PathVariable UUID semesterId) {
    return semesterService.findCourses(semesterId).stream().map(DtoMapper::toDto).toList();
  }
}
