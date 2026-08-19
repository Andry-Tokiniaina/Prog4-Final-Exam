package hei.school.exam.repository;

import hei.school.exam.entity.TrackSemesterCourse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackSemesterCourseRepository extends JpaRepository<TrackSemesterCourse, UUID> {
  List<TrackSemesterCourse> findByTrackIdAndSemesterId(UUID trackId, UUID semesterId);

  List<TrackSemesterCourse> findByTrackId(UUID trackId);

  List<TrackSemesterCourse> findBySemesterId(UUID semesterId);

  List<TrackSemesterCourse> findByCourseId(UUID courseId);

  void deleteByTrackIdAndSemesterId(UUID trackId, UUID semesterId);
}
