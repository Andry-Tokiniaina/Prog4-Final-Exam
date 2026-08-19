package hei.school.exam.service.event;

import hei.school.exam.dto.ExamInput;
import hei.school.exam.entity.Course;
import hei.school.exam.entity.Exam;
import hei.school.exam.entity.Teacher;
import hei.school.exam.entity.User;
import hei.school.exam.repository.CourseRepository;
import hei.school.exam.repository.ExamRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExamService {
  private final ExamRepository examRepository;
  private final CourseRepository courseRepository;

  public List<Exam> findAllByCourse(UUID courseId) {
    return examRepository.findAll().stream()
        .filter(e -> e.getCourse() != null && e.getCourse().getId().equals(courseId))
        .toList();
  }

  public Exam findById(UUID id) {
    return examRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Exam not found: " + id));
  }

  private Exam map(ExamInput input) {
    Exam exam = new Exam();
    exam.setDate(input.date());
    exam.setCoefficient(input.coefficient());
    return exam;
  }

  private void checkTeacherOwnsExam(Exam exam, User principal) {
    if (principal instanceof Teacher teacher) {
      boolean teaches =
          exam.getCourse() != null
              && teacher.getCourses() != null
              && teacher.getCourses().stream()
                  .anyMatch(c -> c.getId().equals(exam.getCourse().getId()));
      if (!teaches) throw new AccessDeniedException("Not your course");
    }
  }

  public Exam getForUser(UUID examId, User principal) {
    Exam exam = findById(examId);
    checkTeacherOwnsExam(exam, principal);
    return exam;
  }

  @Transactional
  public Exam create(UUID courseId, ExamInput input, User principal) {
    Course course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));
    if (principal instanceof Teacher teacher) {
      if (course.getTeachers() == null || !course.getTeachers().contains(teacher))
        throw new AccessDeniedException("Not your course");
    }
    Exam exam = map(input);
    exam.setCourse(course);
    return examRepository.save(exam);
  }

  @Transactional
  public Exam update(UUID id, ExamInput input, User principal) {
    Exam exam = findById(id);
    checkTeacherOwnsExam(exam, principal);
    exam.setDate(input.date());
    exam.setCoefficient(input.coefficient());
    return examRepository.save(exam);
  }

  @Transactional
  public void delete(UUID id) {
    examRepository.delete(findById(id));
  }
}
