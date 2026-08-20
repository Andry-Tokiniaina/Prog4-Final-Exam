package hei.school.exam.repository;

import hei.school.exam.entity.Course;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {
    @Query("select c from Course c left join fetch c.teachers where c.id = :id")
    Optional<Course> findByIdWithTeachers(@Param("id") UUID id);
}
