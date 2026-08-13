package hei.school.exam.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeRepository extends JpaRepository<GradeRepository, UUID> {}
