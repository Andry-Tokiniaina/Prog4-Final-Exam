package hei.school.exam.service.event;

import hei.school.exam.entity.Cohort;
import hei.school.exam.entity.Group;
import hei.school.exam.entity.Student;
import hei.school.exam.entity.Track;
import hei.school.exam.repository.CohortRepository;
import hei.school.exam.repository.GroupRepository;
import hei.school.exam.repository.TrackRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupService {

  private final GroupRepository groupRepository;
  private final CohortRepository cohortRepository;
  private final TrackRepository trackRepository;
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

  @Transactional
  public Group assignCohort(UUID groupId, UUID cohortId) {
    Group group = findById(groupId);

    Cohort cohort =
        cohortRepository
            .findById(cohortId)
            .orElseThrow(() -> new RuntimeException("Cohort not found: " + cohortId));

    group.setCohort(cohort);

    return groupRepository.save(group);
  }

  @Transactional
  public Group assignTrack(UUID groupId, UUID trackId) {
    Group group = findById(groupId);

    Track track =
        trackRepository
            .findById(trackId)
            .orElseThrow(() -> new RuntimeException("Track not found: " + trackId));

    group.setTrack(track);

    return groupRepository.save(group);
  }
}
