package hei.school.exam.dto;

public record TeacherUpdateInput(
    String firstName, String lastName, String email, String password) {}
