package hei.school.exam.repository;

import hei.school.exam.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SemesterRepository extends JpaRepository<Grade, UUID> {}
