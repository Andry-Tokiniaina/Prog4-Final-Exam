package hei.school.exam.service.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hei.school.exam.entity.Course;
import hei.school.exam.entity.Exam;
import hei.school.exam.entity.Grade;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AverageServiceTest {

  private GradeService gradeService;
  private AverageService averageService;

  @BeforeEach
  void setUp() {
    gradeService = mock(GradeService.class);
    averageService = new AverageService(gradeService);
  }

  private Grade gradeOf(double value, double coefficient) {
    Course course = new Course();
    course.setId(UUID.randomUUID());
    Exam exam = new Exam();
    exam.setId(UUID.randomUUID());
    exam.setCoefficient(coefficient);
    exam.setCourse(course);
    Grade grade = new Grade();
    grade.setId(UUID.randomUUID());
    grade.setValue(value);
    grade.setExam(exam);
    return grade;
  }

  @Test
  void returns_zero_when_no_grades() {
    UUID studentId = UUID.randomUUID();
    when(gradeService.findByStudent(studentId)).thenReturn(List.of());

    double result = averageService.calculateStudentAverage(studentId);

    assertThat(result).isZero();
  }

  @Test
  void computes_weighted_average() {
    UUID studentId = UUID.randomUUID();
    List<Grade> grades = List.of(gradeOf(10, 1), gradeOf(20, 1));
    when(gradeService.findByStudent(studentId)).thenReturn(grades);

    double result = averageService.calculateStudentAverage(studentId);

    assertThat(result).isEqualTo(15.0);
  }

  @Test
  void weights_by_coefficient() {
    UUID studentId = UUID.randomUUID();
    List<Grade> grades = List.of(gradeOf(10, 1), gradeOf(20, 3));
    when(gradeService.findByStudent(studentId)).thenReturn(grades);

    double result = averageService.calculateStudentAverage(studentId);

    // (10*1 + 20*3) / (1+3) = 70/4 = 17.5
    assertThat(result).isEqualTo(17.5);
  }

  @Test
  void returns_zero_when_total_coefficient_is_zero() {
    UUID studentId = UUID.randomUUID();
    List<Grade> grades = List.of(gradeOf(10, 0), gradeOf(20, 0));
    when(gradeService.findByStudent(studentId)).thenReturn(grades);

    double result = averageService.calculateStudentAverage(studentId);

    assertThat(result).isZero();
  }
}
