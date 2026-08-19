package hei.school.exam.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class Exam {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private double coefficient;
  private LocalDate date;

  @ManyToOne private Course course;
}
