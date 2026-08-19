package hei.school.exam.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(
        name = "track_semester_course",
        uniqueConstraints =
        @UniqueConstraint(columnNames = {"track_id", "semester_id", "course_id"}))
public class TrackSemesterCourse {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    private Track track;

    @ManyToOne(optional = false)
    private Semester semester;

    @ManyToOne(optional = false)
    private Course course;
}
