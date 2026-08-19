package hei.school.exam.service.event;

import hei.school.exam.dto.GroupInput;
import hei.school.exam.entity.Cohort;
import hei.school.exam.entity.Course;
import hei.school.exam.entity.Group;
import hei.school.exam.entity.Student;
import hei.school.exam.entity.Track;
import hei.school.exam.entity.TrackSemesterCourse;
import hei.school.exam.repository.CohortRepository;
import hei.school.exam.repository.GroupRepository;
import hei.school.exam.repository.TrackRepository;
import hei.school.exam.repository.TrackSemesterCourseRepository;
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
  private final TrackSemesterCourseRepository trackSemesterCourseRepository;

  public List<Group> findAll(UUID cohortId, UUID trackId) {
    return groupRepository.findAll().stream()
        .filter(
            g ->
                cohortId == null
                    || (g.getCohort() != null && cohortId.equals(g.getCohort().getId())))
        .filter(
            g -> trackId == null || (g.getTrack() != null && trackId.equals(g.getTrack().getId())))
        .toList();
  }

  public Group findById(UUID id) {
    return groupRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Group not found: " + id));
  }

  private Group map(GroupInput input) {
    Group group = new Group();
    group.setName(input.name());
    if (input.cohortId() != null) {
      Cohort cohort = new Cohort();
      cohort.setId(input.cohortId());
      group.setCohort(cohort);
    }
    if (input.trackId() != null) {
      Track track = new Track();
      track.setId(input.trackId());
      group.setTrack(track);
    }
    return group;
  }

  @Transactional
  public Group create(GroupInput input) {
    Group group = map(input);
    resolveCohortAndTrack(group);
    return groupRepository.save(group);
  }

  @Transactional
  public Group update(UUID id, GroupInput input) {
    Group group = findById(id);
    Group updated = map(input);
    resolveCohortAndTrack(updated);
    group.setName(updated.getName());
    group.setCohort(updated.getCohort());
    group.setTrack(updated.getTrack());
    return groupRepository.save(group);
  }

  private void resolveCohortAndTrack(Group group) {
    if (group.getCohort() != null && group.getCohort().getId() != null) {
      Cohort cohort =
          cohortRepository
              .findById(group.getCohort().getId())
              .orElseThrow(() -> new RuntimeException("Cohort not found"));
      group.setCohort(cohort);
    }
    if (group.getTrack() != null && group.getTrack().getId() != null) {
      Track track =
          trackRepository
              .findById(group.getTrack().getId())
              .orElseThrow(() -> new RuntimeException("Track not found"));
      group.setTrack(track);
    }
  }

  @Transactional
  public void delete(UUID id) {
    Group group = findById(id);
    groupRepository.delete(group);
  }

  public List<Course> findCourses(UUID groupId) {
    Group group = findById(groupId);
    if (group.getTrack() == null) return List.of();
    return trackSemesterCourseRepository.findByTrackId(group.getTrack().getId()).stream()
        .map(TrackSemesterCourse::getCourse)
        .distinct()
        .toList();
  }

  public List<Student> findStudents(UUID groupId) {
    findById(groupId);
    return studentService.findByGroup(groupId);
  }
}
