package hei.school.exam.repository;

import hei.school.exam.entity.Grade;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SemesterRepository extends JpaRepository<Grade, UUID> {}
