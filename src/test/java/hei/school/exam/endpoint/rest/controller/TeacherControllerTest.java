package hei.school.exam.endpoint.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.exam.dto.CourseDto;
import hei.school.exam.dto.TeacherDto;
import hei.school.exam.dto.TeacherInput;
import hei.school.exam.dto.TeacherUpdateInput;
import hei.school.exam.entity.Course;
import hei.school.exam.entity.Teacher;
import hei.school.exam.service.event.TeacherService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TeacherControllerTest {

  private TeacherService teacherService;
  private TeacherController controller;

  @BeforeEach
  void setUp() {
    teacherService = mock(TeacherService.class);
    controller = new TeacherController(teacherService);
  }

  private Teacher teacherWithId(UUID id) {
    Teacher t = new Teacher();
    t.setId(id);
    t.setFirstName("Jean");
    t.setLastName("Dupont");
    t.setEmail("jean@example.com");
    return t;
  }

  @Test
  void list_maps_teachers() {
    when(teacherService.findAll()).thenReturn(List.of(teacherWithId(UUID.randomUUID())));

    assertThat(controller.list()).hasSize(1);
  }

  @Test
  void create_delegates_to_service() {
    TeacherInput input = new TeacherInput("Jean", "Dupont", "jean@example.com", "pwd");
    Teacher created = teacherWithId(UUID.randomUUID());
    when(teacherService.create(input)).thenReturn(created);

    TeacherDto result = controller.create(input);

    assertThat(result.id()).isEqualTo(created.getId());
  }

  @Test
  void get_delegates_to_service() {
    UUID id = UUID.randomUUID();
    when(teacherService.findById(id)).thenReturn(teacherWithId(id));

    assertThat(controller.get(id).id()).isEqualTo(id);
  }

  @Test
  void update_delegates_to_service() {
    UUID id = UUID.randomUUID();
    TeacherUpdateInput input = new TeacherUpdateInput("A", "B", "a@b.com", null);
    when(teacherService.update(id, input)).thenReturn(teacherWithId(id));

    assertThat(controller.update(id, input).id()).isEqualTo(id);
  }

  @Test
  void delete_delegates_to_service() {
    UUID id = UUID.randomUUID();

    controller.delete(id);

    verify(teacherService, times(1)).delete(id);
  }

  @Test
  void myCourses_delegates_using_principal_id() {
    UUID teacherId = UUID.randomUUID();
    Teacher principal = teacherWithId(teacherId);
    Course course = new Course();
    course.setId(UUID.randomUUID());
    when(teacherService.findCourses(teacherId)).thenReturn(List.of(course));

    List<CourseDto> result = controller.myCourses(principal);

    assertThat(result).hasSize(1);
  }
}
