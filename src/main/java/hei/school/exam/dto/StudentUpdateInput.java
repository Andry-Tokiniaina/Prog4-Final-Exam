package hei.school.exam.dto;

import java.util.UUID;

public record StudentUpdateInput(
    String std, String firstName, String lastName, String email, String password, UUID groupId) {}
