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
import hei.school.exam.entity.Track;
import hei.school.exam.entity.TrackSemesterCourse;
import hei.school.exam.repository.CourseRepository;
import hei.school.exam.repository.SemesterRepository;
import hei.school.exam.repository.TrackRepository;
import hei.school.exam.repository.TrackSemesterCourseRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class CourseAssignmentServiceTest {

  private TrackSemesterCourseRepository trackSemesterCourseRepository;
  private TrackRepository trackRepository;
  private SemesterRepository semesterRepository;
  private CourseRepository courseRepository;
  private CourseAssignmentService service;

  @BeforeEach
  void setUp() {
    trackSemesterCourseRepository = mock(TrackSemesterCourseRepository.class);
    trackRepository = mock(TrackRepository.class);
    semesterRepository = mock(SemesterRepository.class);
    courseRepository = mock(CourseRepository.class);
    service =
        new CourseAssignmentService(
            trackSemesterCourseRepository, trackRepository, semesterRepository, courseRepository);
  }

  private Course courseWithCredit(int credit) {
    Course c = new Course();
    c.setId(UUID.randomUUID());
    c.setCredit(credit);
    return c;
  }

  @Test
  void findAssignedCourses_delegates() {
    UUID trackId = UUID.randomUUID();
    UUID semesterId = UUID.randomUUID();
    Course course = courseWithCredit(10);
    TrackSemesterCourse tsc = new TrackSemesterCourse();
    tsc.setCourse(course);
    when(trackSemesterCourseRepository.findByTrackIdAndSemesterId(trackId, semesterId))
        .thenReturn(List.of(tsc));

    assertThat(service.findAssignedCourses(trackId, semesterId)).containsExactly(course);
  }

  @Test
  void replaceAssignment_throws_when_track_missing() {
    UUID trackId = UUID.randomUUID();
    UUID semesterId = UUID.randomUUID();
    when(trackRepository.findById(trackId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.replaceAssignment(trackId, semesterId, List.of()))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Track not found");
  }

  @Test
  void replaceAssignment_throws_when_semester_missing() {
    UUID trackId = UUID.randomUUID();
    UUID semesterId = UUID.randomUUID();
    when(trackRepository.findById(trackId)).thenReturn(Optional.of(new Track()));
    when(semesterRepository.findById(semesterId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.replaceAssignment(trackId, semesterId, List.of()))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Semester not found");
  }

  @Test
  void replaceAssignment_throws_when_some_courses_not_found() {
    UUID trackId = UUID.randomUUID();
    UUID semesterId = UUID.randomUUID();
    UUID courseId1 = UUID.randomUUID();
    UUID courseId2 = UUID.randomUUID();
    when(trackRepository.findById(trackId)).thenReturn(Optional.of(new Track()));
    when(semesterRepository.findById(semesterId)).thenReturn(Optional.of(new Semester()));
    when(courseRepository.findAllById(List.of(courseId1, courseId2)))
        .thenReturn(List.of(courseWithCredit(15)));

    assertThatThrownBy(
            () -> service.replaceAssignment(trackId, semesterId, List.of(courseId1, courseId2)))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("One or more courses were not found");
  }

  @Test
  void replaceAssignment_throws_422_when_credits_not_30() {
    UUID trackId = UUID.randomUUID();
    UUID semesterId = UUID.randomUUID();
    Course course = courseWithCredit(15);
    when(trackRepository.findById(trackId)).thenReturn(Optional.of(new Track()));
    when(semesterRepository.findById(semesterId)).thenReturn(Optional.of(new Semester()));
    when(courseRepository.findAllById(List.of(course.getId()))).thenReturn(List.of(course));

    assertThatThrownBy(
            () -> service.replaceAssignment(trackId, semesterId, List.of(course.getId())))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("422");
  }

  @Test
  void replaceAssignment_saves_when_credits_sum_to_30() {
    UUID trackId = UUID.randomUUID();
    UUID semesterId = UUID.randomUUID();
    Track track = new Track();
    track.setId(trackId);
    Semester semester = new Semester();
    semester.setId(semesterId);
    Course c1 = courseWithCredit(20);
    Course c2 = courseWithCredit(10);
    when(trackRepository.findById(trackId)).thenReturn(Optional.of(track));
    when(semesterRepository.findById(semesterId)).thenReturn(Optional.of(semester));
    when(courseRepository.findAllById(List.of(c1.getId(), c2.getId()))).thenReturn(List.of(c1, c2));

    List<Course> result =
        service.replaceAssignment(trackId, semesterId, List.of(c1.getId(), c2.getId()));

    assertThat(result).containsExactly(c1, c2);
    verify(trackSemesterCourseRepository, times(1))
        .deleteByTrackIdAndSemesterId(trackId, semesterId);
    verify(trackSemesterCourseRepository, times(1)).saveAll(any());
  }

  @Test
  void findTracksForCourse_returns_distinct_tracks() {
    UUID courseId = UUID.randomUUID();
    Track track = new Track();
    track.setId(UUID.randomUUID());
    TrackSemesterCourse tsc1 = new TrackSemesterCourse();
    tsc1.setTrack(track);
    TrackSemesterCourse tsc2 = new TrackSemesterCourse();
    tsc2.setTrack(track);
    when(trackSemesterCourseRepository.findByCourseId(courseId)).thenReturn(List.of(tsc1, tsc2));

    assertThat(service.findTracksForCourse(courseId)).containsExactly(track);
  }
}
