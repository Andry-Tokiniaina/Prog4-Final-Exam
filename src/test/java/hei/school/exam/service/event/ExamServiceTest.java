package hei.school.exam.service.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.exam.dto.ExamInput;
import hei.school.exam.entity.Admin;
import hei.school.exam.entity.Course;
import hei.school.exam.entity.Exam;
import hei.school.exam.entity.Teacher;
import hei.school.exam.repository.CourseRepository;
import hei.school.exam.repository.ExamRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class ExamServiceTest {

  private ExamRepository examRepository;
  private CourseRepository courseRepository;
  private ExamService examService;

  @BeforeEach
  void setUp() {
    examRepository = mock(ExamRepository.class);
    courseRepository = mock(CourseRepository.class);
    examService = new ExamService(examRepository, courseRepository);
  }

  @Test
  void findAllByCourse_filters_by_course_id() {
    UUID courseId = UUID.randomUUID();
    Course course = new Course();
    course.setId(courseId);
    Course otherCourse = new Course();
    otherCourse.setId(UUID.randomUUID());
    Exam matching = new Exam();
    matching.setCourse(course);
    Exam other = new Exam();
    other.setCourse(otherCourse);
    when(examRepository.findAll()).thenReturn(List.of(matching, other));

    assertThat(examService.findAllByCourse(courseId)).containsExactly(matching);
  }

  @Test
  void findById_throws_when_missing() {
    UUID id = UUID.randomUUID();
    when(examRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> examService.findById(id)).isInstanceOf(RuntimeException.class);
  }

  @Test
  void getForUser_denies_teacher_not_teaching_course() {
    UUID examId = UUID.randomUUID();
    Course course = new Course();
    course.setId(UUID.randomUUID());
    Exam exam = new Exam();
    exam.setId(examId);
    exam.setCourse(course);
    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));

    Teacher teacher = new Teacher();
    teacher.setCourses(new ArrayList<>());

    assertThatThrownBy(() -> examService.getForUser(examId, teacher))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void getForUser_allows_teacher_teaching_course() {
    UUID examId = UUID.randomUUID();
    Course course = new Course();
    course.setId(UUID.randomUUID());
    Exam exam = new Exam();
    exam.setId(examId);
    exam.setCourse(course);
    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));

    Teacher teacher = new Teacher();
    teacher.setCourses(new ArrayList<>(List.of(course)));

    assertThat(examService.getForUser(examId, teacher)).isEqualTo(exam);
  }

  @Test
  void getForUser_allows_admin() {
    UUID examId = UUID.randomUUID();
    Exam exam = new Exam();
    exam.setId(examId);
    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));

    assertThat(examService.getForUser(examId, Admin.builder().build())).isEqualTo(exam);
  }

  @Test
  void create_denies_teacher_not_teaching_course() {
    UUID courseId = UUID.randomUUID();
    Course course = new Course();
    course.setId(courseId);
    course.setTeachers(new ArrayList<>());
    when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

    Teacher teacher = new Teacher();

    ExamInput input = new ExamInput(LocalDate.now(), 2.0);

    assertThatThrownBy(() -> examService.create(courseId, input, teacher))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void create_saves_exam_for_admin() {
    UUID courseId = UUID.randomUUID();
    Course course = new Course();
    course.setId(courseId);
    when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
    when(examRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    LocalDate date = LocalDate.of(2026, 1, 1);
    ExamInput input = new ExamInput(date, 3.0);

    Exam created = examService.create(courseId, input, Admin.builder().build());

    assertThat(created.getCourse()).isEqualTo(course);
    assertThat(created.getDate()).isEqualTo(date);
    assertThat(created.getCoefficient()).isEqualTo(3.0);
  }

  @Test
  void create_throws_when_course_missing() {
    UUID courseId = UUID.randomUUID();
    when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

    ExamInput input = new ExamInput(LocalDate.now(), 1.0);

    assertThatThrownBy(() -> examService.create(courseId, input, Admin.builder().build()))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Course not found");
  }

  @Test
  void update_modifies_exam() {
    UUID examId = UUID.randomUUID();
    Exam exam = new Exam();
    exam.setId(examId);
    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
    when(examRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    LocalDate date = LocalDate.of(2026, 5, 1);
    Exam updated = examService.update(examId, new ExamInput(date, 4.0), Admin.builder().build());

    assertThat(updated.getDate()).isEqualTo(date);
    assertThat(updated.getCoefficient()).isEqualTo(4.0);
  }

  @Test
  void delete_removes_found_exam() {
    UUID examId = UUID.randomUUID();
    Exam exam = new Exam();
    exam.setId(examId);
    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));

    examService.delete(examId);

    verify(examRepository, times(1)).delete(exam);
  }
}
