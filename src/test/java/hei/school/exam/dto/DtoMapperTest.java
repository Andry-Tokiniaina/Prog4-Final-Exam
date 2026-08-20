package hei.school.exam.dto;

import static org.assertj.core.api.Assertions.assertThat;

import hei.school.exam.entity.Cohort;
import hei.school.exam.entity.Course;
import hei.school.exam.entity.Exam;
import hei.school.exam.entity.Grade;
import hei.school.exam.entity.GradeHistory;
import hei.school.exam.entity.Group;
import hei.school.exam.entity.Semester;
import hei.school.exam.entity.Student;
import hei.school.exam.entity.Teacher;
import hei.school.exam.entity.Track;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DtoMapperTest {

  @Test
  void toDto_student_with_group_track_cohort() {
    Track track = new Track();
    track.setId(UUID.randomUUID());
    Cohort cohort = new Cohort();
    cohort.setId(UUID.randomUUID());
    Group group = new Group();
    group.setId(UUID.randomUUID());
    group.setTrack(track);
    group.setCohort(cohort);

    Student student = new Student();
    student.setId(UUID.randomUUID());
    student.setRef("STD1");
    student.setFirstName("Jean");
    student.setLastName("Dupont");
    student.setEmail("jean@example.com");
    student.setGroup(group);

    StudentDto dto = DtoMapper.toDto(student);

    assertThat(dto.id()).isEqualTo(student.getId());
    assertThat(dto.ref()).isEqualTo("STD1");
    assertThat(dto.groupId()).isEqualTo(group.getId());
    assertThat(dto.trackId()).isEqualTo(track.getId());
    assertThat(dto.cohortId()).isEqualTo(cohort.getId());
  }

  @Test
  void toDto_student_without_group() {
    Student student = new Student();
    student.setId(UUID.randomUUID());

    StudentDto dto = DtoMapper.toDto(student);

    assertThat(dto.groupId()).isNull();
    assertThat(dto.trackId()).isNull();
    assertThat(dto.cohortId()).isNull();
  }

  @Test
  void toDto_student_with_group_without_track_or_cohort() {
    Group group = new Group();
    group.setId(UUID.randomUUID());
    Student student = new Student();
    student.setId(UUID.randomUUID());
    student.setGroup(group);

    StudentDto dto = DtoMapper.toDto(student);

    assertThat(dto.groupId()).isEqualTo(group.getId());
    assertThat(dto.trackId()).isNull();
    assertThat(dto.cohortId()).isNull();
  }

  @Test
  void toDto_teacher() {
    Teacher teacher = new Teacher();
    teacher.setId(UUID.randomUUID());
    teacher.setFirstName("A");
    teacher.setLastName("B");
    teacher.setEmail("a@b.com");

    TeacherDto dto = DtoMapper.toDto(teacher);

    assertThat(dto.id()).isEqualTo(teacher.getId());
    assertThat(dto.firstName()).isEqualTo("A");
  }

  @Test
  void toDto_track_has_fixed_expected_credits() {
    Track track = new Track();
    track.setId(UUID.randomUUID());
    track.setName("Info");

    TrackDto dto = DtoMapper.toDto(track);

    assertThat(dto.name()).isEqualTo("Info");
    assertThat(dto.expectedTotalCredits()).isEqualTo(180);
  }

  @Test
  void toDto_semester_has_fixed_expected_credits() {
    Semester semester = new Semester();
    semester.setId(UUID.randomUUID());
    semester.setNumber(3);
    semester.setYear(2);

    SemesterDto dto = DtoMapper.toDto(semester);

    assertThat(dto.number()).isEqualTo(3);
    assertThat(dto.year()).isEqualTo(2);
    assertThat(dto.expectedCredits()).isEqualTo(30);
  }

  @Test
  void toDto_course() {
    Course course = new Course();
    course.setId(UUID.randomUUID());
    course.setRef("C1");
    course.setTitle("Algo");
    course.setCredit(5);

    CourseDto dto = DtoMapper.toDto(course);

    assertThat(dto.ref()).isEqualTo("C1");
    assertThat(dto.credit()).isEqualTo(5);
  }

  @Test
  void toDto_exam_with_course() {
    Course course = new Course();
    course.setId(UUID.randomUUID());
    Exam exam = new Exam();
    exam.setId(UUID.randomUUID());
    exam.setCourse(course);
    exam.setDate(LocalDate.of(2026, 1, 1));
    exam.setCoefficient(2.5);

    ExamDto dto = DtoMapper.toDto(exam);

    assertThat(dto.courseId()).isEqualTo(course.getId());
    assertThat(dto.coefficient()).isEqualTo(2.5);
  }

  @Test
  void toDto_exam_without_course() {
    Exam exam = new Exam();
    exam.setId(UUID.randomUUID());

    ExamDto dto = DtoMapper.toDto(exam);

    assertThat(dto.courseId()).isNull();
  }

  @Test
  void toDto_group_with_cohort_and_track() {
    Cohort cohort = new Cohort();
    cohort.setId(UUID.randomUUID());
    Track track = new Track();
    track.setId(UUID.randomUUID());
    Group group = new Group();
    group.setId(UUID.randomUUID());
    group.setName("G1");
    group.setCohort(cohort);
    group.setTrack(track);

    GroupDto dto = DtoMapper.toDto(group);

    assertThat(dto.name()).isEqualTo("G1");
    assertThat(dto.cohortId()).isEqualTo(cohort.getId());
    assertThat(dto.trackId()).isEqualTo(track.getId());
  }

  @Test
  void toDto_group_without_cohort_or_track() {
    Group group = new Group();
    group.setId(UUID.randomUUID());

    GroupDto dto = DtoMapper.toDto(group);

    assertThat(dto.cohortId()).isNull();
    assertThat(dto.trackId()).isNull();
  }

  @Test
  void toDto_grade_with_student_and_exam() {
    Student student = new Student();
    student.setId(UUID.randomUUID());
    Exam exam = new Exam();
    exam.setId(UUID.randomUUID());
    Grade grade = new Grade();
    grade.setId(UUID.randomUUID());
    grade.setStudent(student);
    grade.setExam(exam);
    grade.setValue(15);
    grade.setUpdatedAt(Instant.EPOCH);

    GradeDto dto = DtoMapper.toDto(grade);

    assertThat(dto.studentId()).isEqualTo(student.getId());
    assertThat(dto.examId()).isEqualTo(exam.getId());
    assertThat(dto.value()).isEqualTo(15);
  }

  @Test
  void toDto_grade_without_student_or_exam() {
    Grade grade = new Grade();
    grade.setId(UUID.randomUUID());

    GradeDto dto = DtoMapper.toDto(grade);

    assertThat(dto.studentId()).isNull();
    assertThat(dto.examId()).isNull();
  }

  @Test
  void toDto_gradeHistory_with_changedBy() {
    Teacher changedBy = new Teacher();
    changedBy.setId(UUID.randomUUID());
    GradeHistory history = new GradeHistory();
    history.setId(UUID.randomUUID());
    history.setPreviousValue(10);
    history.setNewValue(15);
    history.setReason("correction");
    history.setChangedBy(changedBy);
    history.setChangedAt(Instant.EPOCH);

    GradeHistoryEntryDto dto = DtoMapper.toDto(history);

    assertThat(dto.previousValue()).isEqualTo(10);
    assertThat(dto.newValue()).isEqualTo(15);
    assertThat(dto.changedBy()).isEqualTo(changedBy.getId());
  }

  @Test
  void toDto_gradeHistory_without_changedBy() {
    GradeHistory history = new GradeHistory();
    history.setId(UUID.randomUUID());

    GradeHistoryEntryDto dto = DtoMapper.toDto(history);

    assertThat(dto.changedBy()).isNull();
  }

  @Test
  void toDto_cohort() {
    Cohort cohort = new Cohort();
    cohort.setId(UUID.randomUUID());
    cohort.setName("Promo 2026");
    cohort.setStartDate(LocalDate.of(2023, 9, 1));
    cohort.setEndDate(LocalDate.of(2026, 6, 30));

    CohortDto dto = DtoMapper.toDto(cohort);

    assertThat(dto.name()).isEqualTo("Promo 2026");
    assertThat(dto.startDate()).isEqualTo(LocalDate.of(2023, 9, 1));
  }
}
