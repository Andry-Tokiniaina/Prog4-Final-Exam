package hei.school.exam.service.event;

import hei.school.exam.dto.TrackInput;
import hei.school.exam.entity.Course;
import hei.school.exam.entity.Track;
import hei.school.exam.repository.TrackRepository;
import hei.school.exam.repository.TrackSemesterCourseRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TrackService {

  private final TrackRepository trackRepository;
  private final TrackSemesterCourseRepository trackSemesterCourseRepository;

  public List<Track> findAll() {
    return trackRepository.findAll();
  }

  public Track findById(UUID id) {
    return trackRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Track not found: " + id));
  }

  @Transactional
  public Track create(TrackInput input) {
    Track track = new Track();
    track.setName(input.name());
    return trackRepository.save(track);
  }

  @Transactional
  public Track update(UUID id, TrackInput input) {
    Track track = findById(id);
    track.setName(input.name());

    return trackRepository.save(track);
  }

  @Transactional
  public void delete(UUID id) {
    Track track = findById(id);
    trackRepository.delete(track);
  }

  /** Aggregate of every course assigned to this track, across all of its semesters. */
  public List<Course> findCourses(UUID trackId) {
    findById(trackId);
    return trackSemesterCourseRepository.findByTrackId(trackId).stream()
        .map(hei.school.exam.entity.TrackSemesterCourse::getCourse)
        .distinct()
        .toList();
  }
}
