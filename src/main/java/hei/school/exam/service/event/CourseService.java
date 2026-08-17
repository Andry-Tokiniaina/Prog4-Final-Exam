package hei.school.exam.service.event;

import hei.school.exam.entity.Course;
import hei.school.exam.entity.Teacher;
import hei.school.exam.repository.CourseRepository;
import hei.school.exam.repository.TeacherRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseService {

  private final CourseRepository courseRepository;
  private final TeacherRepository teacherRepository;

  public List<Course> findAll() {
    return courseRepository.findAll();
  }

  public Course findById(UUID id) {
    return courseRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Course not found: " + id));
  }

  @Transactional
  public Course create(Course course) {
    return courseRepository.save(course);
  }

  @Transactional
  public Course update(UUID id, Course updatedCourse) {
    Course course = findById(id);

    course.setRef(updatedCourse.getRef());
    course.setTitle(updatedCourse.getTitle());
    course.setCredit(updatedCourse.getCredit());

    return courseRepository.save(course);
  }

  @Transactional
  public void delete(UUID id) {
    Course course = findById(id);
    courseRepository.delete(course);
  }

  public List<Teacher> findTeachers(UUID courseId) {
    Course course = findById(courseId);

    if (course.getTeachers() == null) {
      return List.of();
    }

    return course.getTeachers();
  }

  @Transactional
  public void assignTeacher(UUID courseId, UUID teacherId) {
    Course course = findById(courseId);

    Teacher teacher =
        teacherRepository
            .findById(teacherId)
            .orElseThrow(() -> new RuntimeException("Teacher not found: " + teacherId));

    if (course.getTeachers() == null) {
      course.setTeachers(new ArrayList<>());
    }

    if (!course.getTeachers().contains(teacher)) {
      course.getTeachers().add(teacher);
    }

    if (teacher.getCourses() == null) {
      teacher.setCourses(new ArrayList<>());
    }

    if (!teacher.getCourses().contains(course)) {
      teacher.getCourses().add(course);
    }

    courseRepository.save(course);
    teacherRepository.save(teacher);
  }

  @Transactional
  public void removeTeacher(UUID courseId, UUID teacherId) {
    Course course = findById(courseId);

    Teacher teacher =
        teacherRepository
            .findById(teacherId)
            .orElseThrow(() -> new RuntimeException("Teacher not found: " + teacherId));

    if (course.getTeachers() != null) {
      course.getTeachers().remove(teacher);
    }

    if (teacher.getCourses() != null) {
      teacher.getCourses().remove(course);
    }

    courseRepository.save(course);
    teacherRepository.save(teacher);
  }
}
