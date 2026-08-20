package hei.school.exam.endpoint.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.exam.dto.CourseDto;
import hei.school.exam.dto.CourseInput;
import hei.school.exam.dto.ExamDto;
import hei.school.exam.dto.ExamInput;
import hei.school.exam.dto.IdRefInput;
import hei.school.exam.dto.TeacherDto;
import hei.school.exam.dto.TrackDto;
import hei.school.exam.entity.Admin;
import hei.school.exam.entity.Course;
import hei.school.exam.entity.Exam;
import hei.school.exam.entity.Teacher;
import hei.school.exam.entity.Track;
import hei.school.exam.service.event.CourseAssignmentService;
import hei.school.exam.service.event.CourseService;
import hei.school.exam.service.event.ExamService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CourseControllerTest {

  private CourseService courseService;
  private ExamService examService;
  private CourseAssignmentService courseAssignmentService;
  private CourseController controller;

  @BeforeEach
  void setUp() {
    courseService = mock(CourseService.class);
    examService = mock(ExamService.class);
    courseAssignmentService = mock(CourseAssignmentService.class);
    controller = new CourseController(courseService, examService, courseAssignmentService);
  }

  private Course courseWithId(UUID id) {
    Course c = new Course();
    c.setId(id);
    c.setRef("C1");
    c.setTitle("Algo");
    c.setCredit(5);
    return c;
  }

  @Test
  void list_maps_courses_to_dtos() {
    Course course = courseWithId(UUID.randomUUID());
    when(courseService.findAll()).thenReturn(List.of(course));

    List<CourseDto> result = controller.list();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).ref()).isEqualTo("C1");
  }

  @Test
  void create_delegates_to_service() {
    CourseInput input = new CourseInput("C2", "Maths", 4);
    Course created = courseWithId(UUID.randomUUID());
    when(courseService.create(input)).thenReturn(created);

    CourseDto result = controller.create(input);

    assertThat(result.id()).isEqualTo(created.getId());
  }

  @Test
  void get_uses_getForUser() {
    UUID courseId = UUID.randomUUID();
    Course course = courseWithId(courseId);
    Admin admin = Admin.builder().build();
    when(courseService.getForUser(courseId, admin)).thenReturn(course);

    CourseDto result = controller.get(courseId, admin);

    assertThat(result.id()).isEqualTo(courseId);
  }

  @Test
  void update_delegates_to_service() {
    UUID courseId = UUID.randomUUID();
    CourseInput input = new CourseInput("C3", "Physique", 6);
    Course updated = courseWithId(courseId);
    when(courseService.update(courseId, input)).thenReturn(updated);

    CourseDto result = controller.update(courseId, input);

    assertThat(result.id()).isEqualTo(courseId);
  }

  @Test
  void delete_delegates_to_service() {
    UUID courseId = UUID.randomUUID();

    controller.delete(courseId);

    verify(courseService, times(1)).delete(courseId);
  }

  @Test
  void teachers_maps_to_dtos() {
    UUID courseId = UUID.randomUUID();
    Teacher teacher = new Teacher();
    teacher.setId(UUID.randomUUID());
    when(courseService.findTeachers(courseId)).thenReturn(List.of(teacher));

    List<TeacherDto> result = controller.teachers(courseId);

    assertThat(result).hasSize(1);
  }

  @Test
  void assignTeacher_delegates_with_teacherId() {
    UUID courseId = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();

    controller.assignTeacher(courseId, new IdRefInput(courseId, teacherId));

    verify(courseService, times(1)).assignTeacher(courseId, teacherId);
  }

  @Test
  void removeTeacher_delegates_to_service() {
    UUID courseId = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();

    controller.removeTeacher(courseId, teacherId);

    verify(courseService, times(1)).removeTeacher(courseId, teacherId);
  }

  @Test
  void tracks_maps_to_dtos() {
    UUID courseId = UUID.randomUUID();
    Track track = new Track();
    track.setId(UUID.randomUUID());
    when(courseAssignmentService.findTracksForCourse(courseId)).thenReturn(List.of(track));

    List<TrackDto> result = controller.tracks(courseId);

    assertThat(result).hasSize(1);
  }

  @Test
  void exams_checks_access_then_lists_exams() {
    UUID courseId = UUID.randomUUID();
    Admin admin = Admin.builder().build();
    Exam exam = new Exam();
    exam.setId(UUID.randomUUID());
    when(examService.findAllByCourse(courseId)).thenReturn(List.of(exam));

    List<ExamDto> result = controller.exams(courseId, admin);

    assertThat(result).hasSize(1);
    verify(courseService, times(1)).getForUser(courseId, admin);
  }

  @Test
  void createExam_delegates_to_service() {
    UUID courseId = UUID.randomUUID();
    Admin admin = Admin.builder().build();
    ExamInput input = new ExamInput(LocalDate.now(), 2.0);
    Exam exam = new Exam();
    exam.setId(UUID.randomUUID());
    when(examService.create(courseId, input, admin)).thenReturn(exam);

    ExamDto result = controller.createExam(courseId, input, admin);

    assertThat(result.id()).isEqualTo(exam.getId());
  }
}
