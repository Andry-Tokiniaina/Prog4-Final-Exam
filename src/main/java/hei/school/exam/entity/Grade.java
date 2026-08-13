package hei.school.exam.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class Grade {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private double value;
  private Instant updatedAt;

  @ManyToOne private Student student;
  @ManyToOne private Exam exam;
}
