package hei.school.exam.service.event;

import hei.school.exam.dto.CourseInput;
import hei.school.exam.entity.Course;
import hei.school.exam.entity.Teacher;
import hei.school.exam.entity.User;
import hei.school.exam.repository.CourseRepository;
import hei.school.exam.repository.TeacherRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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

  private Course map(CourseInput input) {
    Course course = new Course();
    course.setRef(input.ref());
    course.setTitle(input.title());
    course.setCredit(input.credit());
    return course;
  }

  @Transactional
  public Course create(CourseInput input) {
    return courseRepository.save(map(input));
  }

  @Transactional
  public Course update(UUID id, CourseInput input) {
    Course course = findById(id);
    Course updated = map(input);
    course.setRef(updated.getRef());
    course.setTitle(updated.getTitle());
    course.setCredit(updated.getCredit());
    return courseRepository.save(course);
  }

  @Transactional
  public void delete(UUID id) {
    courseRepository.delete(findById(id));
  }

  @Transactional(readOnly = true)
  public List<Teacher> findTeachers(UUID courseId) {
    Course course =
        courseRepository
            .findByIdWithTeachers(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));
    return course.getTeachers() == null ? List.of() : new ArrayList<>(course.getTeachers());
  }

  @Transactional
  public void assignTeacher(UUID courseId, UUID teacherId) {
    Course course = findById(courseId);
    Teacher teacher =
        teacherRepository
            .findById(teacherId)
            .orElseThrow(() -> new RuntimeException("Teacher not found: " + teacherId));
    if (course.getTeachers() == null) course.setTeachers(new ArrayList<>());
    if (!course.getTeachers().contains(teacher)) course.getTeachers().add(teacher);
    if (teacher.getCourses() == null) teacher.setCourses(new ArrayList<>());
    if (!teacher.getCourses().contains(course)) teacher.getCourses().add(course);
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
    if (course.getTeachers() != null) course.getTeachers().remove(teacher);
    if (teacher.getCourses() != null) teacher.getCourses().remove(course);
    courseRepository.save(course);
    teacherRepository.save(teacher);
  }

  @Transactional(readOnly = true)
  public void checkTeacherOnCourse(Teacher teacher, Course course) {
    Course fresh =
        courseRepository
            .findByIdWithTeachers(course.getId())
            .orElseThrow(() -> new RuntimeException("Course not found: " + course.getId()));
    if (fresh.getTeachers() == null || !fresh.getTeachers().contains(teacher)) {
      throw new AccessDeniedException("Not your course");
    }
  }

  @Transactional(readOnly = true)
  public Course getForUser(UUID courseId, User principal) {
    Course course = findById(courseId);
    if (principal instanceof Teacher teacher) checkTeacherOnCourse(teacher, course);
    return course;
  }
}
