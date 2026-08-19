package hei.school.exam.service.event;

import hei.school.exam.dto.CohortResultsDto;
import hei.school.exam.dto.GraduateEntryDto;
import hei.school.exam.entity.Cohort;
import hei.school.exam.entity.Grade;
import hei.school.exam.entity.Student;
import hei.school.exam.repository.CohortRepository;
import hei.school.exam.repository.StudentRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CohortService {

  private static final double GRADUATION_THRESHOLD = 10.0;

  private final CohortRepository cohortRepository;
  private final StudentRepository studentRepository;
  private final GradeService gradeService;

  public List<Cohort> findAll() {
    return cohortRepository.findAll();
  }

  public Cohort findById(UUID id) {
    return cohortRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Cohort not found: " + id));
  }

  public CohortResultsDto computeResults(UUID cohortId) {
    Cohort cohort = findById(cohortId);
    List<Student> students = findStudentsInCohort(cohortId);

    List<Double> averages = students.stream().map(this::calculateAverage).toList();

    long graduatedCount = averages.stream().filter(avg -> avg >= GRADUATION_THRESHOLD).count();

    double overallAverage =
            averages.stream().mapToDouble(Double::doubleValue).average().orElse(0);

    // Cohort n'a qu'une seule startDate -> une seule entrée dans la liste.
    int year = cohort.getStartDate().getYear();
    List<CohortResultsDto.YearAverage> averageByYear =
            List.of(new CohortResultsDto.YearAverage(year, overallAverage));

    return new CohortResultsDto(cohortId, averageByYear, graduatedCount, students.size());
  }

  public List<GraduateEntryDto> findGraduates(UUID cohortId) {
    List<Student> students = findStudentsInCohort(cohortId);

    List<Student> graduated =
            students.stream()
                    .filter(student -> calculateAverage(student) >= GRADUATION_THRESHOLD)
                    .sorted(Comparator.comparingDouble(this::calculateAverage).reversed())
                    .toList();

    List<GraduateEntryDto> result = new ArrayList<>();
    int rank = 1;
    for (Student student : graduated) {
      result.add(
              new GraduateEntryDto(
                      rank++,
                      student.getRef(),
                      student.getLastName(),
                      student.getFirstName(),
                      calculateAverage(student)));
    }
    return result;
  }

  private List<Student> findStudentsInCohort(UUID cohortId) {
    return studentRepository.findAll().stream()
            .filter(
                    student ->
                            student.getGroup() != null
                                    && student.getGroup().getCohort() != null
                                    && student.getGroup().getCohort().getId().equals(cohortId))
            .toList();
  }

  private double calculateAverage(Student student) {
    List<Grade> grades = gradeService.findByStudent(student.getId());

    if (grades.isEmpty()) {
      return 0;
    }

    double weightedSum = 0;
    double coefficients = 0;

    for (Grade grade : grades) {
      double coefficient = grade.getExam().getCoefficient();
      weightedSum += grade.getValue() * coefficient;
      coefficients += coefficient;
    }

    if (coefficients == 0) {
      return 0;
    }

    return weightedSum / coefficients;
  }
}