package hei.school.exam.dto;

import java.util.UUID;

public record GroupDto(UUID id, String name, UUID cohortId, UUID trackId) {}
