package hei.school.exam.dto;

import java.util.UUID;

public record SemesterDto(UUID id, int number, int year, int expectedCredits) {}
