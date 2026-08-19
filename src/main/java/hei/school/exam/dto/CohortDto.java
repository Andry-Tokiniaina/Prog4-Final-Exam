package hei.school.exam.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CohortDto(UUID id, String name, LocalDate startDate, LocalDate endDate) {}
