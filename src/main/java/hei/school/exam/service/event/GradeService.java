package hei.school.exam.service.event;

import hei.school.exam.entity.Exam;
import hei.school.exam.entity.Grade;
import hei.school.exam.entity.GradeHistory;
import hei.school.exam.entity.Student;
import hei.school.exam.entity.Teacher;
import hei.school.exam.entity.User;
import hei.school.exam.repository.ExamRepository;
import hei.school.exam.repository.GradeHistoryRepository;
import hei.school.exam.repository.GradeRepository;
import hei.school.exam.repository.StudentRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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

  private boolean teaches(Teacher teacher, Exam exam) {
    return exam != null
        && exam.getCourse() != null
        && teacher.getCourses() != null
        && teacher.getCourses().stream().anyMatch(c -> c.getId().equals(exam.getCourse().getId()));
  }

  private void checkAccess(Grade grade, User principal) {
    if (principal instanceof Teacher teacher && !teaches(teacher, grade.getExam()))
      throw new AccessDeniedException("Not your course");
    if (principal instanceof Student student
        && (grade.getStudent() == null || !grade.getStudent().getId().equals(student.getId())))
      throw new AccessDeniedException("Not your grade");
  }

  public List<Grade> findByStudent(UUID studentId) {
    return gradeRepository.findAll().stream()
        .filter(g -> g.getStudent() != null && g.getStudent().getId().equals(studentId))
        .toList();
  }

  public List<Grade> findByStudentForUser(UUID studentId, User principal) {
    List<Grade> grades = findByStudent(studentId);
    if (principal instanceof Teacher teacher)
      return grades.stream().filter(g -> teaches(teacher, g.getExam())).toList();
    return grades;
  }

  public List<Grade> findByCourseForUser(UUID courseId, User principal) {
    if (principal instanceof Teacher teacher
        && (teacher.getCourses() == null
            || teacher.getCourses().stream().noneMatch(c -> c.getId().equals(courseId))))
      throw new AccessDeniedException("Not your course");
    return gradeRepository.findAll().stream()
        .filter(
            g ->
                g.getExam() != null
                    && g.getExam().getCourse() != null
                    && g.getExam().getCourse().getId().equals(courseId))
        .toList();
  }

  public List<Grade> findByExamForUser(UUID examId, User principal) {
    Exam exam =
        examRepository.findById(examId).orElseThrow(() -> new RuntimeException("Exam not found"));
    if (principal instanceof Teacher teacher && !teaches(teacher, exam))
      throw new AccessDeniedException("Not your course");
    return findByExam(examId);
  }

  public List<Grade> findByExam(UUID examId) {
    return gradeRepository.findAll().stream()
        .filter(g -> g.getExam() != null && g.getExam().getId().equals(examId))
        .toList();
  }

  @Transactional
  public Grade create(UUID studentId, UUID examId, double value, User principal) {
    Exam exam =
        examRepository.findById(examId).orElseThrow(() -> new RuntimeException("Exam not found"));
    if (principal instanceof Teacher teacher && !teaches(teacher, exam))
      throw new AccessDeniedException("Not your course");
    Student student =
        studentRepository
            .findById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found"));
    Grade grade = new Grade();
    grade.setStudent(student);
    grade.setExam(exam);
    grade.setValue(value);
    grade.setUpdatedAt(Instant.now());
    return gradeRepository.save(grade);
  }

  @Transactional
  public Grade update(UUID id, double newValue, String reason, User changedBy) {
    Grade grade = findById(id);
    checkAccess(grade, changedBy);
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

  public Grade getForUser(UUID id, User principal) {
    Grade grade = findById(id);
    checkAccess(grade, principal);
    return grade;
  }

  public List<GradeHistory> findHistoryForUser(UUID gradeId, User principal) {
    Grade grade = findById(gradeId);
    checkAccess(grade, principal);
    return gradeHistoryRepository.findByGradeIdOrderByChangedAtDesc(gradeId);
  }
}
