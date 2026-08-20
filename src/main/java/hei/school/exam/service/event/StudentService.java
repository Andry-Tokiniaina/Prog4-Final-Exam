package hei.school.exam.service.event;

import hei.school.exam.dto.StudentInput;
import hei.school.exam.dto.StudentUpdateInput;
import hei.school.exam.entity.Course;
import hei.school.exam.entity.Grade;
import hei.school.exam.entity.Group;
import hei.school.exam.entity.Student;
import hei.school.exam.entity.Teacher;
import hei.school.exam.entity.TrackSemesterCourse;
import hei.school.exam.repository.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentService {

  private final StudentRepository studentRepository;
  private final GroupRepository groupRepository;
  private final GradeRepository gradeRepository;
  private final TrackSemesterCourseRepository trackSemesterCourseRepository;
  private final PasswordEncoder passwordEncoder;
  private final TeacherRepository teacherRepository;

  public List<Student> findAll(UUID cohortId, UUID groupId, UUID trackId) {
    return studentRepository.findAll().stream()
        .filter(
            s -> groupId == null || (s.getGroup() != null && groupId.equals(s.getGroup().getId())))
        .filter(
            s ->
                cohortId == null
                    || (s.getGroup() != null
                        && s.getGroup().getCohort() != null
                        && cohortId.equals(s.getGroup().getCohort().getId())))
        .filter(
            s ->
                trackId == null
                    || (s.getGroup() != null
                        && s.getGroup().getTrack() != null
                        && trackId.equals(s.getGroup().getTrack().getId())))
        .toList();
  }

  public Student findById(UUID id) {
    return studentRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Student not found: " + id));
  }

  private Student map(StudentInput input) {
    Student student =
        Student.builder()
            .ref(input.std())
            .firstName(input.firstName())
            .lastName(input.lastName())
            .email(input.email())
            .password(passwordEncoder.encode(input.password()))
            .build();
    if (input.groupId() != null) {
      Group group = new Group();
      group.setId(input.groupId());
      student.setGroup(group);
    }
    return student;
  }

  @Transactional
  public Student create(StudentInput input) {
    Student student = map(input);
    if (student.getGroup() != null && student.getGroup().getId() != null) {
      student.setGroup(
          groupRepository
              .findById(student.getGroup().getId())
              .orElseThrow(
                  () -> new RuntimeException("Group not found: " + student.getGroup().getId())));
    }
    return studentRepository.save(student);
  }

  @Transactional
  public Student update(UUID id, StudentUpdateInput input) {
    Student student = findById(id);
    if (input.std() != null) {
      student.setRef(input.std());
    }
    if (input.firstName() != null) {
      student.setFirstName(input.firstName());
    }
    if (input.lastName() != null) {
      student.setLastName(input.lastName());
    }
    if (input.email() != null) {
      student.setEmail(input.email());
    }
    if (input.password() != null && !input.password().isBlank()) {
      student.setPassword(passwordEncoder.encode(input.password()));
    }
    if (input.groupId() != null) {
      student.setGroup(
          groupRepository
              .findById(input.groupId())
              .orElseThrow(() -> new RuntimeException("Group not found: " + input.groupId())));
    }
    return studentRepository.save(student);
  }

  @Transactional(readOnly = true)
  public void checkTeacherTeachesStudent(Teacher teacher, Student student) {
    if (student.getGroup() == null || student.getGroup().getTrack() == null)
      throw new AccessDeniedException("Student is not in a taught group");

    Teacher freshTeacher =
            teacherRepository
                    .findByIdWithCourses(teacher.getId())
                    .orElseThrow(() -> new RuntimeException("Teacher not found: " + teacher.getId()));
    Set<UUID> teacherCourseIds =
            freshTeacher.getCourses() == null
                    ? Set.of()
                    : freshTeacher.getCourses().stream().map(Course::getId).collect(Collectors.toSet());

    boolean teaches =
            trackSemesterCourseRepository.findByTrackId(student.getGroup().getTrack().getId()).stream()
                    .map(TrackSemesterCourse::getCourse)
                    .anyMatch(course -> teacherCourseIds.contains(course.getId()));

    if (!teaches) throw new AccessDeniedException("Teacher does not teach this student");
  }

  @Transactional(readOnly = true)
  public Student getForUser(UUID id, hei.school.exam.entity.User principal) {
    Student student = findById(id);
    if (principal instanceof Teacher teacher) checkTeacherTeachesStudent(teacher, student);
    return student;
  }

  public boolean hasDiploma(UUID studentId) {
    Student student = findById(studentId);
    if (student.getGroup() == null || student.getGroup().getTrack() == null) return false;
    List<Course> courses =
        trackSemesterCourseRepository.findByTrackId(student.getGroup().getTrack().getId()).stream()
            .map(TrackSemesterCourse::getCourse)
            .distinct()
            .toList();
    List<Grade> grades =
        gradeRepository.findAll().stream()
            .filter(g -> g.getStudent() != null && studentId.equals(g.getStudent().getId()))
            .toList();
    if (courses.isEmpty()) return false;
    return courses.stream()
        .allMatch(
            course -> {
              List<Grade> courseGrades =
                  grades.stream()
                      .filter(
                          g ->
                              g.getExam() != null
                                  && g.getExam().getCourse() != null
                                  && course.getId().equals(g.getExam().getCourse().getId()))
                      .toList();
              if (courseGrades.isEmpty()) return false;
              double totalCoefficient =
                  courseGrades.stream().mapToDouble(g -> g.getExam().getCoefficient()).sum();
              if (totalCoefficient == 0) return false;
              double average =
                  courseGrades.stream()
                          .mapToDouble(g -> g.getValue() * g.getExam().getCoefficient())
                          .sum()
                      / totalCoefficient;
              return average >= 10;
            });
  }

  @Transactional
  public void delete(UUID id) {
    studentRepository.delete(findById(id));
  }

  @Transactional
  public Student changeGroup(UUID studentId, UUID groupId) {
    Student student = findById(studentId);
    Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new RuntimeException("Group not found: " + groupId));
    student.setGroup(group);
    return studentRepository.save(student);
  }

  public List<Student> findByGroup(UUID groupId) {
    return studentRepository.findAll().stream()
        .filter(s -> s.getGroup() != null && s.getGroup().getId().equals(groupId))
        .toList();
  }
}
