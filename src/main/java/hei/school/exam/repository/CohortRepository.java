package hei.school.exam.repository;

import hei.school.exam.entity.Cohort;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CohortRepository extends JpaRepository<Cohort, UUID> {}