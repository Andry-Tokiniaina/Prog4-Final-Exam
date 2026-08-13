package hei.school.exam.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class Student extends User {
    private String ref;
    @ManyToOne(cascade = CascadeType.ALL)
    private Group group;
}
