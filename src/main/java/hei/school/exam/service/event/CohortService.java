package hei.school.exam.service.event;

import hei.school.exam.dto.CohortResultsDto;
import hei.school.exam.dto.GraduateEntryDto;
import hei.school.exam.entity.Cohort;
import hei.school.exam.entity.Course;
import hei.school.exam.entity.Grade;
import hei.school.exam.entity.Group;
import hei.school.exam.entity.Student;
import hei.school.exam.entity.TrackSemesterCourse;
import hei.school.exam.repository.CohortRepository;
import hei.school.exam.repository.GradeRepository;
import hei.school.exam.repository.GroupRepository;
import hei.school.exam.repository.StudentRepository;
import hei.school.exam.repository.TrackSemesterCourseRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Computes cohort-wide results (yearly averages, graduate list). A student is considered graduated
 * once their average is >= 10 in EVERY course of their current track, regardless of which group
 * they were in when each grade was obtained (group history is not tracked, only the grades
 * themselves, which is enough since a student keeps every grade they ever earned).
 */
@Service
@RequiredArgsConstructor
public class CohortService {

  private static final double GRADUATION_THRESHOLD = 10.0;

  private final CohortRepository cohortRepository;
  private final GroupRepository groupRepository;
  private final StudentRepository studentRepository;
  private final GradeRepository gradeRepository;
  private final TrackSemesterCourseRepository trackSemesterCourseRepository;

  public List<Cohort> findAll() {
    return cohortRepository.findAll();
  }

  public Cohort findById(UUID id) {
    return cohortRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Cohort not found: " + id));
  }

  private List<Student> studentsOfCohort(UUID cohortId) {
    List<UUID> groupIds =
        groupRepository.findAll().stream()
            .filter(g -> g.getCohort() != null && cohortId.equals(g.getCohort().getId()))
            .map(Group::getId)
            .toList();

    return studentRepository.findAll().stream()
        .filter(s -> s.getGroup() != null && groupIds.contains(s.getGroup().getId()))
        .toList();
  }

  /** Weighted (by exam coefficient) average of a student's grades restricted to one course. */
  private Double averageForCourse(Student student, UUID courseId, List<Grade> studentGrades) {
    List<Grade> courseGrades =
        studentGrades.stream()
            .filter(
                g ->
                    g.getExam() != null
                        && g.getExam().getCourse() != null
                        && courseId.equals(g.getExam().getCourse().getId()))
            .toList();

    if (courseGrades.isEmpty()) {
      return null;
    }

    double weightedSum = 0;
    double totalCoefficient = 0;
    for (Grade g : courseGrades) {
      double coefficient = g.getExam().getCoefficient();
      weightedSum += g.getValue() * coefficient;
      totalCoefficient += coefficient;
    }

    return totalCoefficient == 0 ? null : weightedSum / totalCoefficient;
  }

  /** Overall average of a student, credit-weighted across the courses of their track. */
  private Double overallAverage(
      Student student, List<Course> trackCourses, List<Grade> studentGrades) {
    double weightedSum = 0;
    int totalCredits = 0;
    for (Course course : trackCourses) {
      Double courseAverage = averageForCourse(student, course.getId(), studentGrades);
      if (courseAverage != null) {
        weightedSum += courseAverage * course.getCredit();
        totalCredits += course.getCredit();
      }
    }
    return totalCredits == 0 ? null : weightedSum / totalCredits;
  }

  private boolean isGraduated(
      Student student, List<Course> trackCourses, List<Grade> studentGrades) {
    if (trackCourses.isEmpty()) {
      return false;
    }
    for (Course course : trackCourses) {
      Double courseAverage = averageForCourse(student, course.getId(), studentGrades);
      if (courseAverage == null || courseAverage < GRADUATION_THRESHOLD) {
        return false;
      }
    }
    return true;
  }

  public List<GraduateEntryDto> findGraduates(UUID cohortId) {
    findById(cohortId);
    List<Student> students = studentsOfCohort(cohortId);
    List<Grade> allGrades = gradeRepository.findAll();

    Map<UUID, List<Grade>> gradesByStudent =
        allGrades.stream()
            .filter(g -> g.getStudent() != null)
            .collect(Collectors.groupingBy(g -> g.getStudent().getId()));

    record Candidate(Student student, double average) {}

    List<Candidate> graduates =
        students.stream()
            .filter(s -> s.getGroup() != null && s.getGroup().getTrack() != null)
            .map(
                s -> {
                  List<Course> trackCourses =
                      trackSemesterCourseRepository
                          .findByTrackId(s.getGroup().getTrack().getId())
                          .stream()
                          .map(TrackSemesterCourse::getCourse)
                          .distinct()
                          .toList();
                  List<Grade> studentGrades = gradesByStudent.getOrDefault(s.getId(), List.of());

                  if (!isGraduated(s, trackCourses, studentGrades)) {
                    return null;
                  }
                  Double average = overallAverage(s, trackCourses, studentGrades);
                  return average == null ? null : new Candidate(s, average);
                })
            .filter(java.util.Objects::nonNull)
            .sorted(Comparator.comparingDouble(Candidate::average).reversed())
            .toList();

    return java.util.stream.IntStream.range(0, graduates.size())
        .mapToObj(
            i -> {
              Candidate c = graduates.get(i);
              return new GraduateEntryDto(
                  i + 1,
                  c.student().getRef(),
                  c.student().getLastName(),
                  c.student().getFirstName(),
                  c.average());
            })
        .toList();
  }

  public CohortResultsDto computeResults(UUID cohortId) {
    findById(cohortId);
    List<Student> students = studentsOfCohort(cohortId);
    List<Grade> allGrades = gradeRepository.findAll();

    Map<UUID, List<Grade>> gradesByStudent =
        allGrades.stream()
            .filter(g -> g.getStudent() != null)
            .collect(Collectors.groupingBy(g -> g.getStudent().getId()));

    List<CohortResultsDto.YearAverage> averageByYear =
        java.util.stream.IntStream.rangeClosed(1, 3)
            .mapToObj(
                year -> {
                  List<Double> studentYearAverages =
                      students.stream()
                          .map(
                              s -> {
                                List<Grade> studentGrades =
                                    gradesByStudent.getOrDefault(s.getId(), List.of()).stream()
                                        .filter(
                                            g ->
                                                g.getExam() != null
                                                    && g.getExam().getCourse() != null
                                                    && courseIsInYear(
                                                        g.getExam().getCourse().getId(), year))
                                        .toList();
                                if (studentGrades.isEmpty()) {
                                  return null;
                                }
                                double weightedSum = 0;
                                double totalCoefficient = 0;
                                for (Grade g : studentGrades) {
                                  double coefficient = g.getExam().getCoefficient();
                                  weightedSum += g.getValue() * coefficient;
                                  totalCoefficient += coefficient;
                                }
                                return totalCoefficient == 0
                                    ? null
                                    : weightedSum / totalCoefficient;
                              })
                          .filter(java.util.Objects::nonNull)
                          .toList();

                  double yearAverage =
                      studentYearAverages.isEmpty()
                          ? 0
                          : studentYearAverages.stream()
                              .mapToDouble(Double::doubleValue)
                              .average()
                              .orElse(0);

                  return new CohortResultsDto.YearAverage(year, yearAverage);
                })
            .toList();

    long graduatedCount = findGraduates(cohortId).size();

    return new CohortResultsDto(cohortId, averageByYear, graduatedCount, students.size());
  }

  private boolean courseIsInYear(UUID courseId, int year) {
    return trackSemesterCourseRepository.findByCourseId(courseId).stream()
        .anyMatch(tsc -> tsc.getSemester() != null && tsc.getSemester().getYear() == year);
  }
}
