package hei.school.exam.endpoint.rest.controller;

import hei.school.exam.dto.CourseAssignmentInput;
import hei.school.exam.dto.CourseDto;
import hei.school.exam.dto.DtoMapper;
import hei.school.exam.dto.TrackDto;
import hei.school.exam.dto.TrackInput;
import hei.school.exam.entity.Track;
import hei.school.exam.service.event.CourseAssignmentService;
import hei.school.exam.service.event.TrackService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TrackController {

  private final TrackService trackService;
  private final CourseAssignmentService courseAssignmentService;

  @GetMapping("/tracks")
  @PreAuthorize("hasRole('ADMIN')")
  public List<TrackDto> list() {
    return trackService.findAll().stream().map(DtoMapper::toDto).toList();
  }

  @PostMapping("/tracks")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.CREATED)
  public TrackDto create(@RequestBody TrackInput input) {
    Track track = new Track();
    track.setName(input.name());
    return DtoMapper.toDto(trackService.create(track));
  }

  @GetMapping("/tracks/{trackId}")
  @PreAuthorize("hasRole('ADMIN')")
  public TrackDto get(@PathVariable UUID trackId) {
    return DtoMapper.toDto(trackService.findById(trackId));
  }

  @PutMapping("/tracks/{trackId}")
  @PreAuthorize("hasRole('ADMIN')")
  public TrackDto update(@PathVariable UUID trackId, @RequestBody TrackInput input) {
    Track track = new Track();
    track.setName(input.name());
    return DtoMapper.toDto(trackService.update(trackId, track));
  }

  @DeleteMapping("/tracks/{trackId}")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID trackId) {
    trackService.delete(trackId);
  }

  @GetMapping("/tracks/{trackId}/courses")
  @PreAuthorize("hasRole('ADMIN')")
  public List<CourseDto> courses(@PathVariable UUID trackId) {
    return trackService.findCourses(trackId).stream().map(DtoMapper::toDto).toList();
  }

  @GetMapping("/tracks/{trackId}/semesters/{semesterId}/courses")
  @PreAuthorize("hasRole('ADMIN')")
  public List<CourseDto> assignedCourses(@PathVariable UUID trackId, @PathVariable UUID semesterId) {
    return courseAssignmentService.findAssignedCourses(trackId, semesterId).stream()
        .map(DtoMapper::toDto)
        .toList();
  }

  @PutMapping("/tracks/{trackId}/semesters/{semesterId}/courses")
  @PreAuthorize("hasRole('ADMIN')")
  public List<CourseDto> replaceAssignedCourses(
      @PathVariable UUID trackId,
      @PathVariable UUID semesterId,
      @RequestBody CourseAssignmentInput input) {
    return courseAssignmentService.replaceAssignment(trackId, semesterId, input.courseIds()).stream()
        .map(DtoMapper::toDto)
        .toList();
  }
}
