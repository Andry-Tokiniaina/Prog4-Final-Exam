package hei.school.exam.service.event;

import hei.school.exam.entity.Track;
import hei.school.exam.repository.TrackRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TrackService {

    private final TrackRepository trackRepository;

    public List<Track> findAll() {
        return trackRepository.findAll();
    }

    public Track findById(UUID id) {
        return trackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Track not found: " + id));
    }

    @Transactional
    public Track create(Track track) {
        return trackRepository.save(track);
    }

    @Transactional
    public Track update(UUID id, Track updatedTrack) {
        Track track = findById(id);

        track.setName(updatedTrack.getName());

        return trackRepository.save(track);
    }

    @Transactional
    public void delete(UUID id) {
        Track track = findById(id);
        trackRepository.delete(track);
    }
}