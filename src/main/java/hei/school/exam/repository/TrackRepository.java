package hei.school.exam.repository;

import hei.school.exam.entity.Track;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackRepository extends JpaRepository<Track, UUID> {}
