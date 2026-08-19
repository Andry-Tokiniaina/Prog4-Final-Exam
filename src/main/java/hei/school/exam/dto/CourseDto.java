package hei.school.exam.dto;

import java.util.UUID;

public record CourseDto(UUID id, String ref, String title, int credit) {}
