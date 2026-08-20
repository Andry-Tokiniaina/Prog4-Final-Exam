package hei.school.exam.repository;

import hei.school.exam.entity.Teacher;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, UUID> {
    @Query("select t from Teacher t left join fetch t.courses where t.id = :id")

    Optional<Teacher> findByIdWithCourses(@Param("id") UUID id);
}
