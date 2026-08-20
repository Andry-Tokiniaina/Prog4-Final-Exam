package hei.school.exam.service.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.exam.entity.Course;
import hei.school.exam.entity.Semester;
import hei.school.exam.entity.TrackSemesterCourse;
import hei.school.exam.repository.SemesterRepository;
import hei.school.exam.repository.TrackSemesterCourseRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SemesterServiceTest {

  private SemesterRepository semesterRepository;
  private TrackSemesterCourseRepository trackSemesterCourseRepository;
  private SemesterService semesterService;

  @BeforeEach
  void setUp() {
    semesterRepository = mock(SemesterRepository.class);
    trackSemesterCourseRepository = mock(TrackSemesterCourseRepository.class);
    semesterService = new SemesterService(semesterRepository, trackSemesterCourseRepository);
  }

  @Test
  void findAll_delegates() {
    List<Semester> semesters = List.of(new Semester());
    when(semesterRepository.findAll()).thenReturn(semesters);

    assertThat(semesterService.findAll()).isEqualTo(semesters);
  }

  @Test
  void findById_throws_when_missing() {
    UUID id = UUID.randomUUID();
    when(semesterRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> semesterService.findById(id)).isInstanceOf(RuntimeException.class);
  }

  @Test
  void create_computes_year_for_semesters_1_and_2() {
    when(semesterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    assertThat(semesterService.create(1).getYear()).isEqualTo(1);
    assertThat(semesterService.create(2).getYear()).isEqualTo(1);
  }

  @Test
  void create_computes_year_for_semesters_3_and_4() {
    when(semesterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    assertThat(semesterService.create(3).getYear()).isEqualTo(2);
    assertThat(semesterService.create(4).getYear()).isEqualTo(2);
  }

  @Test
  void create_computes_year_for_semesters_5_and_6() {
    when(semesterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    assertThat(semesterService.create(5).getYear()).isEqualTo(3);
    assertThat(semesterService.create(6).getYear()).isEqualTo(3);
  }

  @Test
  void delete_removes_found_semester() {
    UUID id = UUID.randomUUID();
    Semester semester = new Semester();
    semester.setId(id);
    when(semesterRepository.findById(id)).thenReturn(Optional.of(semester));

    semesterService.delete(id);

    verify(semesterRepository, times(1)).delete(semester);
  }

  @Test
  void findCourses_returns_distinct_courses() {
    UUID semesterId = UUID.randomUUID();
    Semester semester = new Semester();
    semester.setId(semesterId);
    when(semesterRepository.findById(semesterId)).thenReturn(Optional.of(semester));

    Course course = new Course();
    course.setId(UUID.randomUUID());
    TrackSemesterCourse tsc = new TrackSemesterCourse();
    tsc.setCourse(course);
    when(trackSemesterCourseRepository.findBySemesterId(semesterId)).thenReturn(List.of(tsc));

    assertThat(semesterService.findCourses(semesterId)).containsExactly(course);
  }
}
