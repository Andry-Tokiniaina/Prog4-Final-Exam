package hei.school.exam.dto;

import java.time.Instant;
import java.util.UUID;

public record GradeHistoryEntryDto(
    UUID id,
    double previousValue,
    double newValue,
    String reason,
    UUID changedBy,
    Instant changedAt) {}
