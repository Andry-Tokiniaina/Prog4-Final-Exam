package hei.school.exam.service.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hei.school.exam.entity.Admin;
import hei.school.exam.entity.Course;
import hei.school.exam.entity.Exam;
import hei.school.exam.entity.Grade;
import hei.school.exam.entity.GradeHistory;
import hei.school.exam.entity.Student;
import hei.school.exam.entity.Teacher;
import hei.school.exam.repository.ExamRepository;
import hei.school.exam.repository.GradeHistoryRepository;
import hei.school.exam.repository.GradeRepository;
import hei.school.exam.repository.StudentRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class GradeServiceTest {

  private GradeRepository gradeRepository;
  private StudentRepository studentRepository;
  private ExamRepository examRepository;
  private GradeHistoryRepository gradeHistoryRepository;
  private GradeService gradeService;

  @BeforeEach
  void setUp() {
    gradeRepository = mock(GradeRepository.class);
    studentRepository = mock(StudentRepository.class);
    examRepository = mock(ExamRepository.class);
    gradeHistoryRepository = mock(GradeHistoryRepository.class);
    gradeService =
        new GradeService(
            gradeRepository, studentRepository, examRepository, gradeHistoryRepository);
  }

  private Course courseWithId(UUID id) {
    Course c = new Course();
    c.setId(id);
    return c;
  }

  @Test
  void findById_throws_when_missing() {
    UUID id = UUID.randomUUID();
    when(gradeRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> gradeService.findById(id)).isInstanceOf(RuntimeException.class);
  }

  @Test
  void findByStudent_filters_grades() {
    UUID studentId = UUID.randomUUID();
    Student student = new Student();
    student.setId(studentId);
    Student otherStudent = new Student();
    otherStudent.setId(UUID.randomUUID());
    Grade matching = new Grade();
    matching.setStudent(student);
    Grade other = new Grade();
    other.setStudent(otherStudent);
    when(gradeRepository.findAll()).thenReturn(List.of(matching, other));

    assertThat(gradeService.findByStudent(studentId)).containsExactly(matching);
  }

  @Test
  void findByStudentForUser_filters_for_teacher() {
    UUID studentId = UUID.randomUUID();
    Student student = new Student();
    student.setId(studentId);

    UUID courseId = UUID.randomUUID();
    Course course = courseWithId(courseId);
    Exam taughtExam = new Exam();
    taughtExam.setCourse(course);
    Exam notTaughtExam = new Exam();
    notTaughtExam.setCourse(courseWithId(UUID.randomUUID()));

    Grade gradeTaught = new Grade();
    gradeTaught.setStudent(student);
    gradeTaught.setExam(taughtExam);
    Grade gradeNotTaught = new Grade();
    gradeNotTaught.setStudent(student);
    gradeNotTaught.setExam(notTaughtExam);
    when(gradeRepository.findAll()).thenReturn(List.of(gradeTaught, gradeNotTaught));

    Teacher teacher = new Teacher();
    teacher.setCourses(new ArrayList<>(List.of(course)));

    assertThat(gradeService.findByStudentForUser(studentId, teacher)).containsExactly(gradeTaught);
  }

  @Test
  void findByStudentForUser_returns_all_for_admin() {
    UUID studentId = UUID.randomUUID();
    Student student = new Student();
    student.setId(studentId);
    Grade grade = new Grade();
    grade.setStudent(student);
    when(gradeRepository.findAll()).thenReturn(List.of(grade));

    assertThat(gradeService.findByStudentForUser(studentId, Admin.builder().build()))
        .containsExactly(grade);
  }

  @Test
  void findByCourseForUser_denies_teacher_not_teaching() {
    UUID courseId = UUID.randomUUID();
    Teacher teacher = new Teacher();
    teacher.setCourses(new ArrayList<>());

    assertThatThrownBy(() -> gradeService.findByCourseForUser(courseId, teacher))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void findByExam_filters_by_exam_id() {
    UUID examId = UUID.randomUUID();
    Exam exam = new Exam();
    exam.setId(examId);
    Grade matching = new Grade();
    matching.setExam(exam);
    when(gradeRepository.findAll()).thenReturn(List.of(matching));

    assertThat(gradeService.findByExam(examId)).containsExactly(matching);
  }

  @Test
  void findByExamForUser_denies_teacher_not_teaching_exam() {
    UUID examId = UUID.randomUUID();
    Course course = courseWithId(UUID.randomUUID());
    Exam exam = new Exam();
    exam.setId(examId);
    exam.setCourse(course);
    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));

    Teacher teacher = new Teacher();
    teacher.setCourses(new ArrayList<>());

    assertThatThrownBy(() -> gradeService.findByExamForUser(examId, teacher))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void findByExamForUser_throws_when_exam_missing() {
    UUID examId = UUID.randomUUID();
    when(examRepository.findById(examId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> gradeService.findByExamForUser(examId, Admin.builder().build()))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Exam not found");
  }

  @Test
  void create_denies_teacher_not_teaching_exam() {
    UUID examId = UUID.randomUUID();
    Exam exam = new Exam();
    exam.setId(examId);
    exam.setCourse(courseWithId(UUID.randomUUID()));
    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));

    Teacher teacher = new Teacher();
    teacher.setCourses(new ArrayList<>());

    assertThatThrownBy(() -> gradeService.create(UUID.randomUUID(), examId, 10, teacher))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void create_saves_grade_for_admin() {
    UUID examId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    Exam exam = new Exam();
    exam.setId(examId);
    Student student = new Student();
    student.setId(studentId);
    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(gradeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    Grade created = gradeService.create(studentId, examId, 15.5, Admin.builder().build());

    assertThat(created.getStudent()).isEqualTo(student);
    assertThat(created.getExam()).isEqualTo(exam);
    assertThat(created.getValue()).isEqualTo(15.5);
    assertThat(created.getUpdatedAt()).isNotNull();
  }

  @Test
  void create_throws_when_student_missing() {
    UUID examId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    Exam exam = new Exam();
    exam.setId(examId);
    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
    when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> gradeService.create(studentId, examId, 10, Admin.builder().build()))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Student not found");
  }

  @Test
  void update_records_history_and_updates_value() {
    UUID id = UUID.randomUUID();
    Grade grade = new Grade();
    grade.setId(id);
    grade.setValue(10);
    when(gradeRepository.findById(id)).thenReturn(Optional.of(grade));
    when(gradeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    Admin admin = Admin.builder().build();
    Grade updated = gradeService.update(id, 18, "correction", admin);

    assertThat(updated.getValue()).isEqualTo(18);
    org.mockito.Mockito.verify(gradeHistoryRepository).save(any(GradeHistory.class));
  }

  @Test
  void update_denies_teacher_not_teaching_grade_course() {
    UUID id = UUID.randomUUID();
    Exam exam = new Exam();
    exam.setCourse(courseWithId(UUID.randomUUID()));
    Grade grade = new Grade();
    grade.setId(id);
    grade.setExam(exam);
    when(gradeRepository.findById(id)).thenReturn(Optional.of(grade));

    Teacher teacher = new Teacher();
    teacher.setCourses(new ArrayList<>());

    assertThatThrownBy(() -> gradeService.update(id, 12, "reason", teacher))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void update_denies_student_not_owning_grade() {
    UUID id = UUID.randomUUID();
    Student owner = new Student();
    owner.setId(UUID.randomUUID());
    Grade grade = new Grade();
    grade.setId(id);
    grade.setStudent(owner);
    when(gradeRepository.findById(id)).thenReturn(Optional.of(grade));

    Student otherStudent = new Student();
    otherStudent.setId(UUID.randomUUID());

    assertThatThrownBy(() -> gradeService.update(id, 12, "reason", otherStudent))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void getForUser_checks_access() {
    UUID id = UUID.randomUUID();
    Student owner = new Student();
    owner.setId(UUID.randomUUID());
    Grade grade = new Grade();
    grade.setId(id);
    grade.setStudent(owner);
    when(gradeRepository.findById(id)).thenReturn(Optional.of(grade));

    Student otherStudent = new Student();
    otherStudent.setId(UUID.randomUUID());

    assertThatThrownBy(() -> gradeService.getForUser(id, otherStudent))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void findHistoryForUser_returns_history_when_access_allowed() {
    UUID id = UUID.randomUUID();
    Grade grade = new Grade();
    grade.setId(id);
    when(gradeRepository.findById(id)).thenReturn(Optional.of(grade));
    List<GradeHistory> history = List.of(new GradeHistory());
    when(gradeHistoryRepository.findByGradeIdOrderByChangedAtDesc(id)).thenReturn(history);

    assertThat(gradeService.findHistoryForUser(id, Admin.builder().build())).isEqualTo(history);
  }
}
