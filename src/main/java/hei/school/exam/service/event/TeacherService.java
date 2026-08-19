package hei.school.exam.service.event;

import hei.school.exam.dto.TeacherInput;
import hei.school.exam.dto.TeacherUpdateInput;
import hei.school.exam.entity.Course;
import hei.school.exam.entity.Teacher;
import hei.school.exam.repository.TeacherRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeacherService {

  private final TeacherRepository teacherRepository;
  private final PasswordEncoder passwordEncoder;

  public List<Teacher> findAll() {
    return teacherRepository.findAll();
  }

  public Teacher findById(UUID id) {
    return teacherRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Teacher not found: " + id));
  }

  private Teacher map(TeacherInput input) {
    return Teacher.builder()
        .firstName(input.firstName())
        .lastName(input.lastName())
        .email(input.email())
        .password(passwordEncoder.encode(input.password()))
        .build();
  }

  @Transactional
  public Teacher create(TeacherInput input) {
    return teacherRepository.save(map(input));
  }

  @Transactional
  public Teacher update(UUID id, TeacherUpdateInput input) {
    Teacher teacher = findById(id);
    teacher.setFirstName(input.firstName());
    teacher.setLastName(input.lastName());
    teacher.setEmail(input.email());
    if (input.password() != null && !input.password().isBlank()) {
      teacher.setPassword(passwordEncoder.encode(input.password()));
    }
    return teacherRepository.save(teacher);
  }

  @Transactional
  public void delete(UUID id) {
    teacherRepository.delete(findById(id));
  }

  @Transactional(readOnly = true)
  public List<Course> findCourses(UUID teacherId) {
    Teacher teacher = findById(teacherId);
    List<Course> courses = teacher.getCourses();
    return courses == null ? List.of() : new ArrayList<>(courses);
  }
}
