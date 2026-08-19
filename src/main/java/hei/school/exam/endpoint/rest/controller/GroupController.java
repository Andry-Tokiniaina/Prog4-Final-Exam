package hei.school.exam.endpoint.rest.controller;

import hei.school.exam.dto.*;
import hei.school.exam.service.event.GroupService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class GroupController {

  private final GroupService groupService;

  @GetMapping("/groups")
  @PreAuthorize("hasRole('ADMIN')")
  public List<GroupDto> list(
      @RequestParam(required = false) UUID cohortId, @RequestParam(required = false) UUID trackId) {
    return groupService.findAll(cohortId, trackId).stream().map(DtoMapper::toDto).toList();
  }

  @PostMapping("/groups")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.CREATED)
  public GroupDto create(@RequestBody GroupInput input) {
    return DtoMapper.toDto(groupService.create(input));
  }

  @GetMapping("/groups/{groupId}")
  @PreAuthorize("hasRole('ADMIN')")
  public GroupDto get(@PathVariable UUID groupId) {
    return DtoMapper.toDto(groupService.findById(groupId));
  }

  @PutMapping("/groups/{groupId}")
  @PreAuthorize("hasRole('ADMIN')")
  public GroupDto update(@PathVariable UUID groupId, @RequestBody GroupInput input) {
    return DtoMapper.toDto(groupService.update(groupId, input));
  }

  @DeleteMapping("/groups/{groupId}")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID groupId) {
    groupService.delete(groupId);
  }

  @GetMapping("/groups/{groupId}/courses")
  @PreAuthorize("hasRole('ADMIN')")
  public List<CourseDto> courses(@PathVariable UUID groupId) {
    return groupService.findCourses(groupId).stream().map(DtoMapper::toDto).toList();
  }

  @GetMapping("/groups/{groupId}/students")
  @PreAuthorize("hasRole('ADMIN')")
  public List<StudentDto> students(@PathVariable UUID groupId) {
    return groupService.findStudents(groupId).stream().map(DtoMapper::toDto).toList();
  }
}
