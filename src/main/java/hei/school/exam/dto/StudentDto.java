package hei.school.exam.dto;

import java.util.UUID;

public record StudentDto(
    UUID id,
    String ref,
    String firstName,
    String lastName,
    String email,
    UUID groupId,
    UUID trackId,
    UUID cohortId) {}
