package hei.school.exam.repository;

import hei.school.exam.entity.Exam;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamRepository extends JpaRepository<Exam, UUID> {}
