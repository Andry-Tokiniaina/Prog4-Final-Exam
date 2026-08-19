package hei.school.exam.dto;

import jakarta.validation.constraints.NotBlank;

public record TeacherInput(
    String firstName, String lastName, String email, @NotBlank String password) {}
