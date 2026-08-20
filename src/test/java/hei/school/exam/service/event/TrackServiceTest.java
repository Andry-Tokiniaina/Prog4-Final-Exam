package hei.school.exam.service.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.exam.dto.TrackInput;
import hei.school.exam.entity.Course;
import hei.school.exam.entity.Track;
import hei.school.exam.entity.TrackSemesterCourse;
import hei.school.exam.repository.TrackRepository;
import hei.school.exam.repository.TrackSemesterCourseRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TrackServiceTest {

  private TrackRepository trackRepository;
  private TrackSemesterCourseRepository trackSemesterCourseRepository;
  private TrackService trackService;

  @BeforeEach
  void setUp() {
    trackRepository = mock(TrackRepository.class);
    trackSemesterCourseRepository = mock(TrackSemesterCourseRepository.class);
    trackService = new TrackService(trackRepository, trackSemesterCourseRepository);
  }

  @Test
  void findAll_delegates() {
    List<Track> tracks = List.of(new Track());
    when(trackRepository.findAll()).thenReturn(tracks);

    assertThat(trackService.findAll()).isEqualTo(tracks);
  }

  @Test
  void findById_throws_when_missing() {
    UUID id = UUID.randomUUID();
    when(trackRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> trackService.findById(id)).isInstanceOf(RuntimeException.class);
  }

  @Test
  void create_saves_track_with_name() {
    when(trackRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    Track created = trackService.create(new TrackInput("Info", "desc"));

    assertThat(created.getName()).isEqualTo("Info");
  }

  @Test
  void update_changes_name() {
    UUID id = UUID.randomUUID();
    Track track = new Track();
    track.setId(id);
    track.setName("Old");
    when(trackRepository.findById(id)).thenReturn(Optional.of(track));
    when(trackRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    Track updated = trackService.update(id, new TrackInput("New", "desc"));

    assertThat(updated.getName()).isEqualTo("New");
  }

  @Test
  void delete_removes_found_track() {
    UUID id = UUID.randomUUID();
    Track track = new Track();
    track.setId(id);
    when(trackRepository.findById(id)).thenReturn(Optional.of(track));

    trackService.delete(id);

    verify(trackRepository, times(1)).delete(track);
  }

  @Test
  void findCourses_aggregates_distinct_courses_across_semesters() {
    UUID trackId = UUID.randomUUID();
    Track track = new Track();
    track.setId(trackId);
    when(trackRepository.findById(trackId)).thenReturn(Optional.of(track));

    Course course = new Course();
    course.setId(UUID.randomUUID());
    TrackSemesterCourse tsc1 = new TrackSemesterCourse();
    tsc1.setCourse(course);
    TrackSemesterCourse tsc2 = new TrackSemesterCourse();
    tsc2.setCourse(course);
    when(trackSemesterCourseRepository.findByTrackId(trackId)).thenReturn(List.of(tsc1, tsc2));

    assertThat(trackService.findCourses(trackId)).containsExactly(course);
  }

  @Test
  void findCourses_throws_when_track_missing() {
    UUID trackId = UUID.randomUUID();
    when(trackRepository.findById(trackId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> trackService.findCourses(trackId))
        .isInstanceOf(RuntimeException.class);
  }
}
