package hei.school.exam.dto;

import java.time.Instant;
import java.util.UUID;

public record GradeDto(UUID id, UUID studentId, UUID examId, double value, Instant updatedAt) {}
