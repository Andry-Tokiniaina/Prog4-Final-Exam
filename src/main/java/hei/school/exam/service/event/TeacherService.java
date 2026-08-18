package hei.school.exam.service.event;

import hei.school.exam.entity.Course;
import hei.school.exam.entity.Teacher;
import hei.school.exam.repository.TeacherRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeacherService {

  private final TeacherRepository teacherRepository;

  public List<Teacher> findAll() {
    return teacherRepository.findAll();
  }

  public Teacher findById(UUID id) {
    return teacherRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Teacher not found: " + id));
  }

  @Transactional
  public Teacher create(Teacher teacher) {
    return teacherRepository.save(teacher);
  }

  @Transactional
  public Teacher update(UUID id, Teacher updatedTeacher) {
    Teacher teacher = findById(id);

    teacher.setFirstName(updatedTeacher.getFirstName());
    teacher.setLastName(updatedTeacher.getLastName());
    teacher.setEmail(updatedTeacher.getEmail());

    return teacherRepository.save(teacher);
  }

  @Transactional
  public void delete(UUID id) {
    Teacher teacher = findById(id);
    teacherRepository.delete(teacher);
  }

  public List<Course> findCourses(UUID teacherId) {
    Teacher teacher = findById(teacherId);
    return teacher.getCourses();
  }
}
