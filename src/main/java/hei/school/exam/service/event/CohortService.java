package hei.school.exam.service.event;

import hei.school.exam.entity.Cohort;
import hei.school.exam.repository.CohortRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CohortService {

    private final CohortRepository cohortRepository;

    public List<Cohort> findAll() {
        return cohortRepository.findAll();
    }

    public Cohort findById(UUID id) {
        return cohortRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cohort not found: " + id));
    }
}