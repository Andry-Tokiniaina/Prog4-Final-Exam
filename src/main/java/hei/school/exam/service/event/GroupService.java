package hei.school.exam.service.event;

import hei.school.exam.entity.Group;
import hei.school.exam.entity.Student;
import hei.school.exam.repository.GroupRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupService {

  private final GroupRepository groupRepository;
  private final StudentService studentService;

  public List<Group> findAll() {
    return groupRepository.findAll();
  }

  public Group findById(UUID id) {
    return groupRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Group not found: " + id));
  }

  @Transactional
  public Group create(Group group) {
    return groupRepository.save(group);
  }

  @Transactional
  public Group update(UUID id, Group updatedGroup) {
    Group group = findById(id);

    group.setName(updatedGroup.getName());
    group.setCohort(updatedGroup.getCohort());
    group.setTrack(updatedGroup.getTrack());

    return groupRepository.save(group);
  }

  @Transactional
  public void delete(UUID id) {
    Group group = findById(id);
    groupRepository.delete(group);
  }

  public List<Student> findStudents(UUID groupId) {
    findById(groupId);
    return studentService.findByGroup(groupId);
  }
}
