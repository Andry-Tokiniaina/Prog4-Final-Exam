package hei.school.exam.service.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hei.school.exam.dto.CohortResultsDto;
import hei.school.exam.dto.GraduateEntryDto;
import hei.school.exam.entity.Cohort;
import hei.school.exam.entity.Course;
import hei.school.exam.entity.Exam;
import hei.school.exam.entity.Grade;
import hei.school.exam.entity.Group;
import hei.school.exam.entity.Semester;
import hei.school.exam.entity.Student;
import hei.school.exam.entity.Track;
import hei.school.exam.entity.TrackSemesterCourse;
import hei.school.exam.repository.CohortRepository;
import hei.school.exam.repository.GradeRepository;
import hei.school.exam.repository.GroupRepository;
import hei.school.exam.repository.StudentRepository;
import hei.school.exam.repository.TrackSemesterCourseRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CohortServiceTest {

  private CohortRepository cohortRepository;
  private GroupRepository groupRepository;
  private StudentRepository studentRepository;
  private GradeRepository gradeRepository;
  private TrackSemesterCourseRepository trackSemesterCourseRepository;
  private StudentService studentService;
  private CohortService cohortService;

  @BeforeEach
  void setUp() {
    cohortRepository = mock(CohortRepository.class);
    groupRepository = mock(GroupRepository.class);
    studentRepository = mock(StudentRepository.class);
    gradeRepository = mock(GradeRepository.class);
    trackSemesterCourseRepository = mock(TrackSemesterCourseRepository.class);
    studentService = mock(StudentService.class);
    cohortService =
        new CohortService(
            cohortRepository,
            groupRepository,
            studentRepository,
            gradeRepository,
            trackSemesterCourseRepository,
            studentService);
  }

  private Cohort cohort(UUID id) {
    Cohort c = new Cohort();
    c.setId(id);
    return c;
  }

  private Group groupOf(UUID id, Cohort cohort, Track track) {
    Group g = new Group();
    g.setId(id);
    g.setCohort(cohort);
    g.setTrack(track);
    return g;
  }

  @Test
  void findAll_delegates() {
    List<Cohort> cohorts = List.of(cohort(UUID.randomUUID()));
    when(cohortRepository.findAll()).thenReturn(cohorts);

    assertThat(cohortService.findAll()).isEqualTo(cohorts);
  }

  @Test
  void findById_throws_when_missing() {
    UUID id = UUID.randomUUID();
    when(cohortRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> cohortService.findById(id)).isInstanceOf(RuntimeException.class);
  }

  @Test
  void findNonGraduates_filters_students_without_diploma() {
    UUID cohortId = UUID.randomUUID();
    Cohort cohort = cohort(cohortId);
    when(cohortRepository.findById(cohortId)).thenReturn(Optional.of(cohort));

    Group group = groupOf(UUID.randomUUID(), cohort, null);
    when(groupRepository.findAll()).thenReturn(List.of(group));

    Student graduated = new Student();
    graduated.setId(UUID.randomUUID());
    graduated.setGroup(group);
    Student notGraduated = new Student();
    notGraduated.setId(UUID.randomUUID());
    notGraduated.setGroup(group);
    when(studentRepository.findAll()).thenReturn(List.of(graduated, notGraduated));

    when(studentService.hasDiploma(graduated.getId())).thenReturn(true);
    when(studentService.hasDiploma(notGraduated.getId())).thenReturn(false);

    assertThat(cohortService.findNonGraduates(cohortId)).containsExactly(notGraduated);
  }

  @Test
  void findGraduates_returns_ranked_list_of_graduated_students() {
    UUID cohortId = UUID.randomUUID();
    Cohort cohort = cohort(cohortId);
    when(cohortRepository.findById(cohortId)).thenReturn(Optional.of(cohort));

    UUID trackId = UUID.randomUUID();
    Track track = new Track();
    track.setId(trackId);
    Group group = groupOf(UUID.randomUUID(), cohort, track);
    when(groupRepository.findAll()).thenReturn(List.of(group));

    Student student = Student.builder().build();
    student.setId(UUID.randomUUID());
    student.setRef("STD1");
    student.setFirstName("Jean");
    student.setLastName("Dupont");
    student.setGroup(group);
    when(studentRepository.findAll()).thenReturn(List.of(student));

    Course course = new Course();
    course.setId(UUID.randomUUID());
    course.setCredit(10);
    TrackSemesterCourse tsc = new TrackSemesterCourse();
    tsc.setCourse(course);
    when(trackSemesterCourseRepository.findByTrackId(trackId)).thenReturn(List.of(tsc));

    Exam exam = new Exam();
    exam.setCourse(course);
    exam.setCoefficient(1);
    Grade grade = new Grade();
    grade.setExam(exam);
    grade.setValue(16);
    grade.setStudent(student);
    when(gradeRepository.findAll()).thenReturn(List.of(grade));

    List<GraduateEntryDto> graduates = cohortService.findGraduates(cohortId);

    assertThat(graduates).hasSize(1);
    assertThat(graduates.get(0).rank()).isEqualTo(1);
    assertThat(graduates.get(0).std()).isEqualTo("STD1");
    assertThat(graduates.get(0).average()).isEqualTo(16.0);
  }

  @Test
  void findGraduates_excludes_students_without_group_or_track() {
    UUID cohortId = UUID.randomUUID();
    Cohort cohort = cohort(cohortId);
    when(cohortRepository.findById(cohortId)).thenReturn(Optional.of(cohort));

    Group group = groupOf(UUID.randomUUID(), cohort, null);
    when(groupRepository.findAll()).thenReturn(List.of(group));

    Student student = new Student();
    student.setId(UUID.randomUUID());
    student.setGroup(group);
    when(studentRepository.findAll()).thenReturn(List.of(student));
    when(gradeRepository.findAll()).thenReturn(List.of());

    assertThat(cohortService.findGraduates(cohortId)).isEmpty();
  }

  @Test
  void computeResults_returns_correct_counts() {
    UUID cohortId = UUID.randomUUID();
    Cohort cohort = cohort(cohortId);
    when(cohortRepository.findById(cohortId)).thenReturn(Optional.of(cohort));

    Group group = groupOf(UUID.randomUUID(), cohort, null);
    when(groupRepository.findAll()).thenReturn(List.of(group));

    Student student = new Student();
    student.setId(UUID.randomUUID());
    student.setGroup(group);
    when(studentRepository.findAll()).thenReturn(List.of(student));
    when(gradeRepository.findAll()).thenReturn(List.of());

    CohortResultsDto results = cohortService.computeResults(cohortId);

    assertThat(results.cohortId()).isEqualTo(cohortId);
    assertThat(results.studentCount()).isEqualTo(1);
    assertThat(results.graduatedCount()).isEqualTo(0);
    assertThat(results.averageByYear()).hasSize(3);
  }

  @Test
  void computeResults_computes_year_average_from_grades() {
    UUID cohortId = UUID.randomUUID();
    Cohort cohort = cohort(cohortId);
    when(cohortRepository.findById(cohortId)).thenReturn(Optional.of(cohort));

    Group group = groupOf(UUID.randomUUID(), cohort, null);
    when(groupRepository.findAll()).thenReturn(List.of(group));

    Student student = new Student();
    student.setId(UUID.randomUUID());
    student.setGroup(group);
    when(studentRepository.findAll()).thenReturn(List.of(student));

    Course course = new Course();
    course.setId(UUID.randomUUID());
    Exam exam = new Exam();
    exam.setCourse(course);
    exam.setCoefficient(1);
    Grade grade = new Grade();
    grade.setExam(exam);
    grade.setValue(12);
    grade.setStudent(student);
    when(gradeRepository.findAll()).thenReturn(List.of(grade));

    Semester semester = new Semester();
    semester.setYear(1);
    TrackSemesterCourse tsc = new TrackSemesterCourse();
    tsc.setSemester(semester);
    when(trackSemesterCourseRepository.findByCourseId(course.getId())).thenReturn(List.of(tsc));

    CohortResultsDto results = cohortService.computeResults(cohortId);

    CohortResultsDto.YearAverage year1 =
        results.averageByYear().stream().filter(y -> y.year() == 1).findFirst().orElseThrow();
    assertThat(year1.average()).isEqualTo(12.0);
  }
}
