package hei.school.exam.endpoint.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.exam.dto.CourseDto;
import hei.school.exam.dto.GroupDto;
import hei.school.exam.dto.GroupInput;
import hei.school.exam.dto.StudentDto;
import hei.school.exam.entity.Course;
import hei.school.exam.entity.Group;
import hei.school.exam.entity.Student;
import hei.school.exam.service.event.GroupService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GroupControllerTest {

  private GroupService groupService;
  private GroupController controller;

  @BeforeEach
  void setUp() {
    groupService = mock(GroupService.class);
    controller = new GroupController(groupService);
  }

  private Group groupWithId(UUID id) {
    Group g = new Group();
    g.setId(id);
    g.setName("G1");
    return g;
  }

  @Test
  void list_delegates_with_filters() {
    UUID cohortId = UUID.randomUUID();
    UUID trackId = UUID.randomUUID();
    when(groupService.findAll(cohortId, trackId))
        .thenReturn(List.of(groupWithId(UUID.randomUUID())));

    List<GroupDto> result = controller.list(cohortId, trackId);

    assertThat(result).hasSize(1);
  }

  @Test
  void create_delegates_to_service() {
    GroupInput input = new GroupInput("G1", null, null);
    Group created = groupWithId(UUID.randomUUID());
    when(groupService.create(input)).thenReturn(created);

    GroupDto result = controller.create(input);

    assertThat(result.id()).isEqualTo(created.getId());
  }

  @Test
  void get_delegates_to_service() {
    UUID id = UUID.randomUUID();
    when(groupService.findById(id)).thenReturn(groupWithId(id));

    assertThat(controller.get(id).id()).isEqualTo(id);
  }

  @Test
  void update_delegates_to_service() {
    UUID id = UUID.randomUUID();
    GroupInput input = new GroupInput("New", null, null);
    when(groupService.update(id, input)).thenReturn(groupWithId(id));

    assertThat(controller.update(id, input).id()).isEqualTo(id);
  }

  @Test
  void delete_delegates_to_service() {
    UUID id = UUID.randomUUID();

    controller.delete(id);

    verify(groupService, times(1)).delete(id);
  }

  @Test
  void courses_maps_to_dtos() {
    UUID id = UUID.randomUUID();
    Course course = new Course();
    course.setId(UUID.randomUUID());
    when(groupService.findCourses(id)).thenReturn(List.of(course));

    List<CourseDto> result = controller.courses(id);

    assertThat(result).hasSize(1);
  }

  @Test
  void students_maps_to_dtos() {
    UUID id = UUID.randomUUID();
    Student student = new Student();
    student.setId(UUID.randomUUID());
    when(groupService.findStudents(id)).thenReturn(List.of(student));

    List<StudentDto> result = controller.students(id);

    assertThat(result).hasSize(1);
  }
}
