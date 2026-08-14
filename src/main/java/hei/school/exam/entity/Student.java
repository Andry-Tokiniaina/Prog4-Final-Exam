package hei.school.exam.entity;

import hei.school.exam.entity.enums.Role;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Data
@DiscriminatorValue("STUDENT")
public class Student extends User {
  private String ref;

  @ManyToOne private Group group;

  @Override
  public Role getRole() {
    return Role.STUDENT;
  }
}
