package hei.school.exam.entity;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class Course {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String ref;
  private String title;
  private int credit;

  @ManyToMany(cascade = CascadeType.ALL)
  @lombok.ToString.Exclude
  @lombok.EqualsAndHashCode.Exclude
  private List<Teacher> teachers;

  @ManyToMany @lombok.ToString.Exclude @lombok.EqualsAndHashCode.Exclude private List<Group> groups;
}
