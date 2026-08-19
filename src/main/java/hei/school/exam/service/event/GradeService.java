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
            examRepository
                    .findById(examId)
                    .orElseThrow(() -> new RuntimeException("Exam not found"));

    Grade grade = new Grade();
    grade.setStudent(student);
    grade.setExam(exam);
    grade.setValue(value);
    grade.setUpdatedAt(Instant.now());

    return gradeRepository.save(grade);
  }

  @Transactional
  public Grade update(UUID id, double value, String reason, User changedBy) {
    Grade grade = findById(id);
    double previousValue = grade.getValue();

    grade.setValue(value);
    grade.setUpdatedAt(Instant.now());
    Grade saved = gradeRepository.save(grade);

    GradeHistory history =
            new GradeHistory(null, saved, previousValue, value, reason, changedBy, Instant.now());
    gradeHistoryRepository.save(history);

    return saved;
  }

  public List<GradeHistory> findHistory(UUID gradeId) {
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