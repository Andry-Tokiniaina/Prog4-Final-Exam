package hei.school.exam.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GradeHistory {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(optional = false)
  private Grade grade;

  private double previousValue;
  private double newValue;

  @Column(length = 1000)
  private String reason;

  @ManyToOne private User changedBy;

  private Instant changedAt;
}
