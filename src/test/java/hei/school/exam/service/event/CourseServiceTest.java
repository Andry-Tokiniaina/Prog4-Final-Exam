package hei.school.exam.service.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.exam.dto.CourseInput;
import hei.school.exam.entity.Course;
import hei.school.exam.entity.Teacher;
import hei.school.exam.repository.CourseRepository;
import hei.school.exam.repository.TeacherRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class CourseServiceTest {

  private CourseRepository courseRepository;
  private TeacherRepository teacherRepository;
  private CourseService courseService;

  @BeforeEach
  void setUp() {
    courseRepository = mock(CourseRepository.class);
    teacherRepository = mock(TeacherRepository.class);
    courseService = new CourseService(courseRepository, teacherRepository);
  }

  @Test
  void findAll_delegates_to_repository() {
    List<Course> courses = List.of(new Course());
    when(courseRepository.findAll()).thenReturn(courses);

    assertThat(courseService.findAll()).isEqualTo(courses);
  }

  @Test
  void findById_returns_course_when_present() {
    UUID id = UUID.randomUUID();
    Course course = new Course();
    course.setId(id);
    when(courseRepository.findById(id)).thenReturn(Optional.of(course));

    assertThat(courseService.findById(id)).isEqualTo(course);
  }

  @Test
  void findById_throws_when_absent() {
    UUID id = UUID.randomUUID();
    when(courseRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> courseService.findById(id))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Course not found");
  }

  @Test
  void create_maps_input_and_saves() {
    CourseInput input = new CourseInput("REF1", "Title", 5);
    when(courseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    Course created = courseService.create(input);

    assertThat(created.getRef()).isEqualTo("REF1");
    assertThat(created.getTitle()).isEqualTo("Title");
    assertThat(created.getCredit()).isEqualTo(5);
  }

  @Test
  void update_modifies_existing_course() {
    UUID id = UUID.randomUUID();
    Course existing = new Course();
    existing.setId(id);
    existing.setRef("OLD");

    when(courseRepository.findById(id)).thenReturn(Optional.of(existing));
    when(courseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    Course updated =
            courseService.update(id, new CourseInput("NEW", "NewTitle", 10));

    assertThat(updated.getId()).isEqualTo(id);
    assertThat(updated.getRef()).isEqualTo("NEW");
    assertThat(updated.getTitle()).isEqualTo("NewTitle");
    assertThat(updated.getCredit()).isEqualTo(10);
  }

  @Test
  void delete_removes_found_course() {
    UUID id = UUID.randomUUID();
    Course course = new Course();
    course.setId(id);

    when(courseRepository.findById(id)).thenReturn(Optional.of(course));

    courseService.delete(id);

    verify(courseRepository, times(1)).delete(course);
  }

  @Test
  void findTeachers_returns_empty_list_when_null() {
    UUID id = UUID.randomUUID();

    Course course = new Course();
    course.setId(id);

    when(courseRepository.findByIdWithTeachers(id))
            .thenReturn(Optional.of(course));

    assertThat(courseService.findTeachers(id)).isEmpty();
  }

  @Test
  void findTeachers_returns_existing_list() {
    UUID id = UUID.randomUUID();

    Teacher teacher = new Teacher();
    teacher.setId(UUID.randomUUID());

    Course course = new Course();
    course.setId(id);
    course.setTeachers(new ArrayList<>(List.of(teacher)));

    when(courseRepository.findByIdWithTeachers(id))
            .thenReturn(Optional.of(course));

    assertThat(courseService.findTeachers(id))
            .containsExactly(teacher);
  }

  @Test
  void assignTeacher_adds_teacher_and_course_both_ways() {
    UUID courseId = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();

    Course course = new Course();
    course.setId(courseId);

    Teacher teacher = new Teacher();
    teacher.setId(teacherId);

    when(courseRepository.findById(courseId))
            .thenReturn(Optional.of(course));
    when(teacherRepository.findById(teacherId))
            .thenReturn(Optional.of(teacher));

    courseService.assignTeacher(courseId, teacherId);

    assertThat(course.getTeachers()).contains(teacher);
    assertThat(teacher.getCourses()).contains(course);

    verify(courseRepository).save(course);
    verify(teacherRepository).save(teacher);
  }

  @Test
  void assignTeacher_does_not_duplicate_existing_link() {
    UUID courseId = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();

    Teacher teacher = new Teacher();
    teacher.setId(teacherId);

    Course course = new Course();
    course.setId(courseId);

    course.setTeachers(new ArrayList<>(List.of(teacher)));
    teacher.setCourses(new ArrayList<>(List.of(course)));

    when(courseRepository.findById(courseId))
            .thenReturn(Optional.of(course));
    when(teacherRepository.findById(teacherId))
            .thenReturn(Optional.of(teacher));

    courseService.assignTeacher(courseId, teacherId);

    assertThat(course.getTeachers()).hasSize(1);
    assertThat(teacher.getCourses()).hasSize(1);
  }

  @Test
  void assignTeacher_throws_when_teacher_missing() {
    UUID courseId = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();

    Course course = new Course();
    course.setId(courseId);

    when(courseRepository.findById(courseId))
            .thenReturn(Optional.of(course));
    when(teacherRepository.findById(teacherId))
            .thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> courseService.assignTeacher(courseId, teacherId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Teacher not found");
  }

  @Test
  void removeTeacher_removes_link_both_ways() {
    UUID courseId = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();

    Teacher teacher = new Teacher();
    teacher.setId(teacherId);

    Course course = new Course();
    course.setId(courseId);
    course.setTeachers(new ArrayList<>(List.of(teacher)));

    teacher.setCourses(new ArrayList<>(List.of(course)));

    when(courseRepository.findById(courseId))
            .thenReturn(Optional.of(course));
    when(teacherRepository.findById(teacherId))
            .thenReturn(Optional.of(teacher));

    courseService.removeTeacher(courseId, teacherId);

    assertThat(course.getTeachers()).isEmpty();
    assertThat(teacher.getCourses()).isEmpty();
  }

  @Test
  void checkTeacherOnCourse_throws_when_not_assigned() {
    UUID courseId = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();

    Teacher teacher = new Teacher();
    teacher.setId(teacherId);

    Course course = new Course();
    course.setId(courseId);
    course.setTeachers(new ArrayList<>());

    when(courseRepository.findByIdWithTeachers(courseId))
            .thenReturn(Optional.of(course));

    assertThatThrownBy(
            () -> courseService.checkTeacherOnCourse(teacher, course))
            .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void checkTeacherOnCourse_passes_when_assigned() {
    UUID courseId = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();

    Teacher teacher = new Teacher();
    teacher.setId(teacherId);

    Course course = new Course();
    course.setId(courseId);
    course.setTeachers(new ArrayList<>(List.of(teacher)));

    when(courseRepository.findByIdWithTeachers(courseId))
            .thenReturn(Optional.of(course));

    courseService.checkTeacherOnCourse(teacher, course);
  }

  @Test
  void getForUser_checks_access_for_teacher_principal() {
    UUID courseId = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();

    Teacher teacher = new Teacher();
    teacher.setId(teacherId);

    Course course = new Course();
    course.setId(courseId);
    course.setTeachers(new ArrayList<>());

    when(courseRepository.findById(courseId))
            .thenReturn(Optional.of(course));
    when(courseRepository.findByIdWithTeachers(courseId))
            .thenReturn(Optional.of(course));

    assertThatThrownBy(
            () -> courseService.getForUser(courseId, teacher))
            .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void getForUser_returns_course_for_non_teacher_principal() {
    UUID courseId = UUID.randomUUID();

    Course course = new Course();
    course.setId(courseId);

    when(courseRepository.findById(courseId))
            .thenReturn(Optional.of(course));

    hei.school.exam.entity.Admin admin =
            hei.school.exam.entity.Admin.builder().build();

    assertThat(courseService.getForUser(courseId, admin))
            .isEqualTo(course);

    verify(courseRepository, never()).save(any());
  }
}