package hei.school.exam.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record StudentInput(
    String std, String firstName, String lastName, String email, @NotBlank String password, UUID groupId) {}
