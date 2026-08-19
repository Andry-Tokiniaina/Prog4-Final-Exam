package hei.school.exam.dto;

import java.util.UUID;

public record TrackDto(UUID id, String name, int expectedTotalCredits) {}
