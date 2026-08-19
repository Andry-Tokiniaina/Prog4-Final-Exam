package hei.school.exam.dto;

import java.util.UUID;

public record GradeInput(UUID studentId, UUID examId, double value) {}
