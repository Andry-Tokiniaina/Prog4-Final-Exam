package hei.school.exam.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import java.util.List;

@Entity
public class Teacher extends User {
  @ManyToMany private List<Course> courses;
}
