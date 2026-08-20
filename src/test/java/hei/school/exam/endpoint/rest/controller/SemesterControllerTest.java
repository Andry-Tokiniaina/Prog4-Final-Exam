package hei.school.exam.endpoint.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.exam.dto.CourseDto;
import hei.school.exam.dto.SemesterDto;
import hei.school.exam.dto.SemesterInput;
import hei.school.exam.entity.Course;
import hei.school.exam.entity.Semester;
import hei.school.exam.service.event.SemesterService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SemesterControllerTest {

  private SemesterService semesterService;
  private SemesterController controller;

  @BeforeEach
  void setUp() {
    semesterService = mock(SemesterService.class);
    controller = new SemesterController(semesterService);
  }

  private Semester semesterWithId(UUID id) {
    Semester s = new Semester();
    s.setId(id);
    s.setNumber(1);
    s.setYear(1);
    return s;
  }

  @Test
  void list_maps_semesters() {
    when(semesterService.findAll()).thenReturn(List.of(semesterWithId(UUID.randomUUID())));

    List<SemesterDto> result = controller.list();

    assertThat(result).hasSize(1);
  }

  @Test
  void create_uses_number_from_input() {
    Semester created = semesterWithId(UUID.randomUUID());
    when(semesterService.create(3)).thenReturn(created);

    SemesterDto result = controller.create(new SemesterInput(3));

    assertThat(result.id()).isEqualTo(created.getId());
  }

  @Test
  void get_delegates_to_service() {
    UUID id = UUID.randomUUID();
    when(semesterService.findById(id)).thenReturn(semesterWithId(id));

    assertThat(controller.get(id).id()).isEqualTo(id);
  }

  @Test
  void delete_delegates_to_service() {
    UUID id = UUID.randomUUID();

    controller.delete(id);

    verify(semesterService, times(1)).delete(id);
  }

  @Test
  void courses_maps_to_dtos() {
    UUID id = UUID.randomUUID();
    Course course = new Course();
    course.setId(UUID.randomUUID());
    when(semesterService.findCourses(id)).thenReturn(List.of(course));

    List<CourseDto> result = controller.courses(id);

    assertThat(result).hasSize(1);
  }
}
