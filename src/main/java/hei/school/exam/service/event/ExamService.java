package hei.school.exam.service.event;

import hei.school.exam.entity.Course;
import hei.school.exam.entity.Exam;
import hei.school.exam.repository.CourseRepository;
import hei.school.exam.repository.ExamRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExamService {

  private final ExamRepository examRepository;
  private final CourseRepository courseRepository;

  public List<Exam> findAllByCourse(UUID courseId) {
    return examRepository.findAll().stream()
        .filter(exam -> exam.getCourse() != null && exam.getCourse().getId().equals(courseId))
        .toList();
  }

  public Exam findById(UUID id) {
    return examRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Exam not found: " + id));
  }

  @Transactional
  public Exam create(UUID courseId, Exam exam) {
    Course course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));

    exam.setCourse(course);

    return examRepository.save(exam);
  }

  @Transactional
  public Exam update(UUID id, Exam updatedExam) {
    Exam exam = findById(id);

    exam.setDate(updatedExam.getDate());
    exam.setCoefficient(updatedExam.getCoefficient());

    return examRepository.save(exam);
  }

  @Transactional
  public void delete(UUID id) {
    Exam exam = findById(id);
    examRepository.delete(exam);
  }
}
