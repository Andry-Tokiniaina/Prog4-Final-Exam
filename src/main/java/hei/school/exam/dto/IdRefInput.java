package hei.school.exam.dto;

import java.util.UUID;

/** Generic {"xxxId": "..."} request body, used for group/teacher assignment endpoints. */
public record IdRefInput(UUID groupId, UUID teacherId) {}
