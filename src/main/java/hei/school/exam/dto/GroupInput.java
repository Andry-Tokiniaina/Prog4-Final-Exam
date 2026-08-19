package hei.school.exam.dto;

import java.util.UUID;

public record GroupInput(String name, UUID cohortId, UUID trackId) {}
