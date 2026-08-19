package hei.school.exam.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ExamDto(UUID id, UUID courseId, LocalDate date, double coefficient) {}
