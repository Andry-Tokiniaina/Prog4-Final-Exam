package hei.school.exam.service.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hei.school.exam.dto.GroupInput;
import hei.school.exam.entity.Cohort;
import hei.school.exam.entity.Course;
import hei.school.exam.entity.Group;
import hei.school.exam.entity.Student;
import hei.school.exam.entity.Track;
import hei.school.exam.entity.TrackSemesterCourse;
import hei.school.exam.repository.CohortRepository;
import hei.school.exam.repository.GroupRepository;
import hei.school.exam.repository.TrackRepository;
import hei.school.exam.repository.TrackSemesterCourseRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GroupServiceTest {

  private GroupRepository groupRepository;
  private CohortRepository cohortRepository;
  private TrackRepository trackRepository;
  private StudentService studentService;
  private TrackSemesterCourseRepository trackSemesterCourseRepository;
  private GroupService groupService;

  @BeforeEach
  void setUp() {
    groupRepository = mock(GroupRepository.class);
    cohortRepository = mock(CohortRepository.class);
    trackRepository = mock(TrackRepository.class);
    studentService = mock(StudentService.class);
    trackSemesterCourseRepository = mock(TrackSemesterCourseRepository.class);
    groupService =
        new GroupService(
            groupRepository,
            cohortRepository,
            trackRepository,
            studentService,
            trackSemesterCourseRepository);
  }

  @Test
  void findAll_filters_by_cohort_and_track() {
    UUID cohortId = UUID.randomUUID();
    UUID trackId = UUID.randomUUID();
    Cohort cohort = new Cohort();
    cohort.setId(cohortId);
    Track track = new Track();
    track.setId(trackId);

    Group matching = new Group();
    matching.setCohort(cohort);
    matching.setTrack(track);
    Group wrongCohort = new Group();
    wrongCohort.setCohort(new Cohort());
    wrongCohort.setTrack(track);

    when(groupRepository.findAll()).thenReturn(List.of(matching, wrongCohort));

    assertThat(groupService.findAll(cohortId, trackId)).containsExactly(matching);
  }

  @Test
  void findAll_returns_all_when_filters_null() {
    Group group = new Group();
    when(groupRepository.findAll()).thenReturn(List.of(group));

    assertThat(groupService.findAll(null, null)).containsExactly(group);
  }

  @Test
  void findById_throws_when_missing() {
    UUID id = UUID.randomUUID();
    when(groupRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> groupService.findById(id)).isInstanceOf(RuntimeException.class);
  }

  @Test
  void create_resolves_cohort_and_track() {
    UUID cohortId = UUID.randomUUID();
    UUID trackId = UUID.randomUUID();
    Cohort cohort = new Cohort();
    cohort.setId(cohortId);
    Track track = new Track();
    track.setId(trackId);
    when(cohortRepository.findById(cohortId)).thenReturn(Optional.of(cohort));
    when(trackRepository.findById(trackId)).thenReturn(Optional.of(track));
    when(groupRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    Group created = groupService.create(new GroupInput("G1", cohortId, trackId));

    assertThat(created.getName()).isEqualTo("G1");
    assertThat(created.getCohort()).isEqualTo(cohort);
    assertThat(created.getTrack()).isEqualTo(track);
  }

  @Test
  void create_without_cohort_or_track() {
    when(groupRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    Group created = groupService.create(new GroupInput("G1", null, null));

    assertThat(created.getCohort()).isNull();
    assertThat(created.getTrack()).isNull();
  }

  @Test
  void create_throws_when_cohort_missing() {
    UUID cohortId = UUID.randomUUID();
    when(cohortRepository.findById(cohortId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> groupService.create(new GroupInput("G1", cohortId, null)))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Cohort not found");
  }

  @Test
  void create_throws_when_track_missing() {
    UUID trackId = UUID.randomUUID();
    when(trackRepository.findById(trackId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> groupService.create(new GroupInput("G1", null, trackId)))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Track not found");
  }

  @Test
  void update_replaces_name_cohort_track() {
    UUID id = UUID.randomUUID();
    Group existing = new Group();
    existing.setId(id);
    when(groupRepository.findById(id)).thenReturn(Optional.of(existing));
    when(groupRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    Group updated = groupService.update(id, new GroupInput("New", null, null));

    assertThat(updated.getName()).isEqualTo("New");
  }

  @Test
  void delete_removes_group() {
    UUID id = UUID.randomUUID();
    Group group = new Group();
    group.setId(id);
    when(groupRepository.findById(id)).thenReturn(Optional.of(group));

    groupService.delete(id);

    org.mockito.Mockito.verify(groupRepository).delete(group);
  }

  @Test
  void findCourses_returns_empty_when_no_track() {
    UUID id = UUID.randomUUID();
    Group group = new Group();
    group.setId(id);
    when(groupRepository.findById(id)).thenReturn(Optional.of(group));

    assertThat(groupService.findCourses(id)).isEmpty();
  }

  @Test
  void findCourses_returns_distinct_courses_from_track() {
    UUID id = UUID.randomUUID();
    UUID trackId = UUID.randomUUID();
    Track track = new Track();
    track.setId(trackId);
    Group group = new Group();
    group.setId(id);
    group.setTrack(track);
    when(groupRepository.findById(id)).thenReturn(Optional.of(group));

    Course course = new Course();
    TrackSemesterCourse tsc = new TrackSemesterCourse();
    tsc.setCourse(course);
    when(trackSemesterCourseRepository.findByTrackId(trackId)).thenReturn(List.of(tsc));

    assertThat(groupService.findCourses(id)).containsExactly(course);
  }

  @Test
  void findStudents_delegates_to_studentService() {
    UUID id = UUID.randomUUID();
    Group group = new Group();
    group.setId(id);
    when(groupRepository.findById(id)).thenReturn(Optional.of(group));
    List<Student> students = List.of(new Student());
    when(studentService.findByGroup(id)).thenReturn(students);

    assertThat(groupService.findStudents(id)).isEqualTo(students);
  }
}
