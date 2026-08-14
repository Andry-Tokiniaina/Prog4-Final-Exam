package hei.school.exam.entity;

import hei.school.exam.entity.enums.Role;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Data
@DiscriminatorValue("TEACHER")
public class Teacher extends User {
  @ManyToMany private List<Course> courses;

  @Override
  public Role getRole() {
    return Role.TEACHER;
  }
}
