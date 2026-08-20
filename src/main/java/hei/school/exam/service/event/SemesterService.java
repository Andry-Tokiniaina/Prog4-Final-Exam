package hei.school.exam.service.event;

import hei.school.exam.entity.Course;
import hei.school.exam.entity.Semester;
import hei.school.exam.repository.SemesterRepository;
import hei.school.exam.repository.TrackSemesterCourseRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SemesterService {

  private final SemesterRepository semesterRepository;
  private final TrackSemesterCourseRepository trackSemesterCourseRepository;

  public List<Semester> findAll() {
    return semesterRepository.findAll();
  }

  public Semester findById(UUID id) {
    return semesterRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Semester not found: " + id));
  }

  @Transactional
  public Semester create(int number) {
    Semester semester = new Semester();
    semester.setNumber(number);
    // Business rule: year = ceil(number / 2), semesters 1-2 => year 1, 3-4 => year 2, 5-6 => year 3
    semester.setYear((number + 1) / 2);
    return semesterRepository.save(semester);
  }

  @Transactional
  public void delete(UUID id) {
    Semester semester = findById(id);
    semesterRepository.delete(semester);
  }

  /** Read-only aggregate of every track/semester assignment for this semester, across tracks. */
  public List<Course> findCourses(UUID semesterId) {
    findById(semesterId);
    return trackSemesterCourseRepository.findBySemesterId(semesterId).stream()
        .map(hei.school.exam.entity.TrackSemesterCourse::getCourse)
        .distinct()
        .toList();
  }
}
