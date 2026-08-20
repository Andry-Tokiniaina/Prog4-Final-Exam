package hei.school.exam.service.event;

import hei.school.exam.entity.Group;
import hei.school.exam.entity.Student;
import hei.school.exam.repository.GroupRepository;
import hei.school.exam.repository.StudentRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentService {

  private final StudentRepository studentRepository;
  private final GroupRepository groupRepository;

  public List<Student> findAll(UUID cohortId, UUID groupId, UUID trackId) {
    return studentRepository.findAll().stream()
        .filter(s -> groupId == null || (s.getGroup() != null && groupId.equals(s.getGroup().getId())))
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

  @Transactional
  public Student create(Student student) {
    if (student.getGroup() != null && student.getGroup().getId() != null) {
      Group group =
          groupRepository
              .findById(student.getGroup().getId())
              .orElseThrow(
                  () -> new RuntimeException("Group not found: " + student.getGroup().getId()));
      student.setGroup(group);
    }
    return studentRepository.save(student);
  }

  @Transactional
  public Student update(UUID id, Student updatedStudent) {
    Student student = findById(id);

    student.setRef(updatedStudent.getRef());
    student.setFirstName(updatedStudent.getFirstName());
    student.setLastName(updatedStudent.getLastName());
    student.setEmail(updatedStudent.getEmail());

    if (updatedStudent.getGroup() != null && updatedStudent.getGroup().getId() != null) {
      Group group =
          groupRepository
              .findById(updatedStudent.getGroup().getId())
              .orElseThrow(() -> new RuntimeException("Group not found"));
      student.setGroup(group);
    }

    return studentRepository.save(student);
  }

  @Transactional
  public void delete(UUID id) {
    Student student = findById(id);
    studentRepository.delete(student);
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
        .filter(student -> student.getGroup() != null && student.getGroup().getId().equals(groupId))
        .toList();
  }
}
