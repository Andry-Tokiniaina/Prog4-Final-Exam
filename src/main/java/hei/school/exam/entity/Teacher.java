package hei.school.exam.entity;

import hei.school.exam.entity.enums.Role;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
@DiscriminatorValue("TEACHER")
public class Teacher extends User {
  @ManyToMany(mappedBy = "teachers")
  @lombok.ToString.Exclude
  @lombok.EqualsAndHashCode.Exclude
  private List<Course> courses;

  @Override
  public Role getRole() {
    return Role.TEACHER;
  }
}
