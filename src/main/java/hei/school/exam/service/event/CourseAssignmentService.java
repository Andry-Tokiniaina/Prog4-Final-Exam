package hei.school.exam.service.event;

import hei.school.exam.entity.Course;
import hei.school.exam.entity.Semester;
import hei.school.exam.entity.Track;
import hei.school.exam.entity.TrackSemesterCourse;
import hei.school.exam.repository.CourseRepository;
import hei.school.exam.repository.SemesterRepository;
import hei.school.exam.repository.TrackRepository;
import hei.school.exam.repository.TrackSemesterCourseRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CourseAssignmentService {

  private static final int EXPECTED_CREDITS_PER_SEMESTER = 30;

  private final TrackSemesterCourseRepository trackSemesterCourseRepository;
  private final TrackRepository trackRepository;
  private final SemesterRepository semesterRepository;
  private final CourseRepository courseRepository;

  public List<Course> findAssignedCourses(UUID trackId, UUID semesterId) {
    return trackSemesterCourseRepository.findByTrackIdAndSemesterId(trackId, semesterId).stream()
        .map(TrackSemesterCourse::getCourse)
        .toList();
  }

  /**
   * Fully replaces the list of courses assigned to a (track, semester) pair. The sum of credits of
   * the provided courses must equal exactly 30, or a 422 is raised.
   */
  @Transactional
  public List<Course> replaceAssignment(UUID trackId, UUID semesterId, List<UUID> courseIds) {
    Track track =
        trackRepository
            .findById(trackId)
            .orElseThrow(() -> new RuntimeException("Track not found"));
    Semester semester =
        semesterRepository
            .findById(semesterId)
            .orElseThrow(() -> new RuntimeException("Semester not found"));

    List<Course> courses = courseRepository.findAllById(courseIds);
    if (courses.size() != courseIds.size()) {
      throw new RuntimeException("One or more courses were not found");
    }

    int totalCredits = courses.stream().mapToInt(Course::getCredit).sum();
    if (totalCredits != EXPECTED_CREDITS_PER_SEMESTER) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY,
          "The sum of credits of the provided courses must equal 30, got " + totalCredits);
    }

    trackSemesterCourseRepository.deleteByTrackIdAndSemesterId(trackId, semesterId);
    trackSemesterCourseRepository.flush();

    List<TrackSemesterCourse> assignments =
        courses.stream()
            .map(course -> new TrackSemesterCourse(null, track, semester, course))
            .toList();
    trackSemesterCourseRepository.saveAll(assignments);

    return courses;
  }

  public List<Track> findTracksForCourse(UUID courseId) {
    return trackSemesterCourseRepository.findByCourseId(courseId).stream()
        .map(TrackSemesterCourse::getTrack)
        .distinct()
        .toList();
  }
}
