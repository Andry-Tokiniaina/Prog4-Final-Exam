package hei.school.exam.endpoint.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.exam.dto.CourseAssignmentInput;
import hei.school.exam.dto.CourseDto;
import hei.school.exam.dto.TrackInput;
import hei.school.exam.entity.Course;
import hei.school.exam.entity.Track;
import hei.school.exam.service.event.CourseAssignmentService;
import hei.school.exam.service.event.TrackService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TrackControllerTest {

  private TrackService trackService;
  private CourseAssignmentService courseAssignmentService;
  private TrackController controller;

  @BeforeEach
  void setUp() {
    trackService = mock(TrackService.class);
    courseAssignmentService = mock(CourseAssignmentService.class);
    controller = new TrackController(trackService, courseAssignmentService);
  }

  private Track trackWithId(UUID id) {
    Track t = new Track();
    t.setId(id);
    t.setName("Info");
    return t;
  }

  @Test
  void list_maps_tracks() {
    when(trackService.findAll()).thenReturn(List.of(trackWithId(UUID.randomUUID())));

    assertThat(controller.list()).hasSize(1);
  }

  @Test
  void create_delegates_to_service() {
    TrackInput input = new TrackInput("Info", "desc");
    Track created = trackWithId(UUID.randomUUID());
    when(trackService.create(input)).thenReturn(created);

    assertThat(controller.create(input).id()).isEqualTo(created.getId());
  }

  @Test
  void get_delegates_to_service() {
    UUID id = UUID.randomUUID();
    when(trackService.findById(id)).thenReturn(trackWithId(id));

    assertThat(controller.get(id).id()).isEqualTo(id);
  }

  @Test
  void update_delegates_to_service() {
    UUID id = UUID.randomUUID();
    TrackInput input = new TrackInput("New", "desc");
    when(trackService.update(id, input)).thenReturn(trackWithId(id));

    assertThat(controller.update(id, input).id()).isEqualTo(id);
  }

  @Test
  void delete_delegates_to_service() {
    UUID id = UUID.randomUUID();

    controller.delete(id);

    verify(trackService, times(1)).delete(id);
  }

  @Test
  void courses_maps_to_dtos() {
    UUID id = UUID.randomUUID();
    Course course = new Course();
    course.setId(UUID.randomUUID());
    when(trackService.findCourses(id)).thenReturn(List.of(course));

    assertThat(controller.courses(id)).hasSize(1);
  }

  @Test
  void assignedCourses_delegates_to_service() {
    UUID trackId = UUID.randomUUID();
    UUID semesterId = UUID.randomUUID();
    Course course = new Course();
    course.setId(UUID.randomUUID());
    when(courseAssignmentService.findAssignedCourses(trackId, semesterId))
        .thenReturn(List.of(course));

    List<CourseDto> result = controller.assignedCourses(trackId, semesterId);

    assertThat(result).hasSize(1);
  }

  @Test
  void replaceAssignedCourses_delegates_with_courseIds() {
    UUID trackId = UUID.randomUUID();
    UUID semesterId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    Course course = new Course();
    course.setId(courseId);
    when(courseAssignmentService.replaceAssignment(trackId, semesterId, List.of(courseId)))
        .thenReturn(List.of(course));

    List<CourseDto> result =
        controller.replaceAssignedCourses(
            trackId, semesterId, new CourseAssignmentInput(List.of(courseId)));

    assertThat(result).hasSize(1);
  }
}
