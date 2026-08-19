package hei.school.exam.endpoint.rest.controller;

import hei.school.exam.dto.*;
import hei.school.exam.entity.User;
import hei.school.exam.service.event.CourseAssignmentService;
import hei.school.exam.service.event.CourseService;
import hei.school.exam.service.event.ExamService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CourseController {
  private final CourseService courseService;
  private final ExamService examService;
  private final CourseAssignmentService courseAssignmentService;

  @GetMapping("/courses")
  @PreAuthorize("hasRole('ADMIN')")
  public List<CourseDto> list() {
    return courseService.findAll().stream().map(DtoMapper::toDto).toList();
  }

  @PostMapping("/courses")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.CREATED)
  public CourseDto create(@RequestBody CourseInput input) {
    return DtoMapper.toDto(courseService.create(input));
  }

  @GetMapping("/courses/{courseId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
  public CourseDto get(@PathVariable UUID courseId, @AuthenticationPrincipal User principal) {
    return DtoMapper.toDto(courseService.getForUser(courseId, principal));
  }

  @PutMapping("/courses/{courseId}")
  @PreAuthorize("hasRole('ADMIN')")
  public CourseDto update(@PathVariable UUID courseId, @RequestBody CourseInput input) {
    return DtoMapper.toDto(courseService.update(courseId, input));
  }

  @DeleteMapping("/courses/{courseId}")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID courseId) {
    courseService.delete(courseId);
  }

  @GetMapping("/courses/{courseId}/teachers")
  @PreAuthorize("hasRole('ADMIN')")
  public List<TeacherDto> teachers(@PathVariable UUID courseId) {
    return courseService.findTeachers(courseId).stream().map(DtoMapper::toDto).toList();
  }

  @PostMapping("/courses/{courseId}/teachers")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void assignTeacher(@PathVariable UUID courseId, @RequestBody IdRefInput body) {
    courseService.assignTeacher(courseId, body.teacherId());
  }

  @DeleteMapping("/courses/{courseId}/teachers/{teacherId}")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeTeacher(@PathVariable UUID courseId, @PathVariable UUID teacherId) {
    courseService.removeTeacher(courseId, teacherId);
  }

  @GetMapping("/courses/{courseId}/tracks")
  @PreAuthorize("hasRole('ADMIN')")
  public List<TrackDto> tracks(@PathVariable UUID courseId) {
    return courseAssignmentService.findTracksForCourse(courseId).stream()
        .map(DtoMapper::toDto)
        .toList();
  }

  @GetMapping("/courses/{courseId}/exams")
  @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
  public List<ExamDto> exams(@PathVariable UUID courseId, @AuthenticationPrincipal User principal) {
    courseService.getForUser(courseId, principal);
    return examService.findAllByCourse(courseId).stream().map(DtoMapper::toDto).toList();
  }

  @PostMapping("/courses/{courseId}/exams")
  @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
  @ResponseStatus(HttpStatus.CREATED)
  public ExamDto createExam(
      @PathVariable UUID courseId,
      @RequestBody ExamInput input,
      @AuthenticationPrincipal User principal) {
    return DtoMapper.toDto(examService.create(courseId, input, principal));
  }
}
