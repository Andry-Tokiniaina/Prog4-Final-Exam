package hei.school.exam.service.event;

import hei.school.exam.entity.Exam;
import hei.school.exam.entity.Grade;
import hei.school.exam.entity.GradeHistory;
import hei.school.exam.entity.Student;
import hei.school.exam.entity.User;
import hei.school.exam.repository.ExamRepository;
import hei.school.exam.repository.GradeHistoryRepository;
import hei.school.exam.repository.GradeRepository;
import hei.school.exam.repository.StudentRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GradeService {

  private final GradeRepository gradeRepository;
  private final StudentRepository studentRepository;
  private final ExamRepository examRepository;
  private final GradeHistoryRepository gradeHistoryRepository;

  public Grade findById(UUID id) {
    return gradeRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Grade not found: " + id));
  }

  @Transactional
  public Grade create(UUID studentId, UUID examId, double value) {

    Student student =
        studentRepository
            .findById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found"));

    Exam exam =
        examRepository.findById(examId).orElseThrow(() -> new RuntimeException("Exam not found"));

    Grade grade = new Grade();
    grade.setStudent(student);
    grade.setExam(exam);
    grade.setValue(value);
    grade.setUpdatedAt(Instant.now());

    return gradeRepository.save(grade);
  }

  /**
   * A grade can change over time (dispute, error correction), but every modification is historised
   * with a mandatory reason.
   */
  @Transactional
  public Grade update(UUID id, double newValue, String reason, User changedBy) {
    Grade grade = findById(id);

    GradeHistory history = new GradeHistory();
    history.setGrade(grade);
    history.setPreviousValue(grade.getValue());
    history.setNewValue(newValue);
    history.setReason(reason);
    history.setChangedBy(changedBy);
    history.setChangedAt(Instant.now());
    gradeHistoryRepository.save(history);

    grade.setValue(newValue);
    grade.setUpdatedAt(Instant.now());

    return gradeRepository.save(grade);
  }

  public List<GradeHistory> findHistory(UUID gradeId) {
    findById(gradeId);
    return gradeHistoryRepository.findByGradeIdOrderByChangedAtDesc(gradeId);
  }

  public List<Grade> findByStudent(UUID studentId) {
    return gradeRepository.findAll().stream()
        .filter(grade -> grade.getStudent() != null && grade.getStudent().getId().equals(studentId))
        .toList();
  }

  public List<Grade> findByExam(UUID examId) {
    return gradeRepository.findAll().stream()
        .filter(grade -> grade.getExam() != null && grade.getExam().getId().equals(examId))
        .toList();
  }

  public List<Grade> findByCourse(UUID courseId) {
    return gradeRepository.findAll().stream()
        .filter(
            grade ->
                grade.getExam() != null
                    && grade.getExam().getCourse() != null
                    && grade.getExam().getCourse().getId().equals(courseId))
        .toList();
  }
}
