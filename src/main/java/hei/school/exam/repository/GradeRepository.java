package hei.school.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GradeRepository extends JpaRepository<GradeRepository, UUID> {
}
