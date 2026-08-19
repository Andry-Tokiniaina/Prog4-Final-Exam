package hei.school.exam.endpoint.rest.controller;

import hei.school.exam.dto.DtoMapper;
import hei.school.exam.dto.StudentDto;
import hei.school.exam.dto.StudentInput;
import hei.school.exam.dto.TranscriptRequestAcceptedDto;
import hei.school.exam.entity.Group;
import hei.school.exam.entity.Student;
import hei.school.exam.entity.Teacher;
import hei.school.exam.entity.User;
import hei.school.exam.service.event.CourseService;
import hei.school.exam.service.event.StudentService;
import hei.school.exam.service.event.TranscriptService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final CourseService courseService;
    private final TranscriptService transcriptService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/students/me")
    @PreAuthorize("hasRole('STUDENT')")
    public StudentDto me(@AuthenticationPrincipal Student principal) {
        return DtoMapper.toDto(studentService.findById(principal.getId()));
    }

    @PostMapping("/students/me/transcript")
    @PreAuthorize("hasRole('STUDENT')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TranscriptRequestAcceptedDto requestMyTranscript(@AuthenticationPrincipal Student principal) {
        transcriptService.requestTranscriptByEmail(principal.getId());
        return new TranscriptRequestAcceptedDto("The transcript will be sent by email shortly.");
    }

    @GetMapping("/students")
    @PreAuthorize("hasRole('ADMIN')")
    public List<StudentDto> list(
            @RequestParam(required = false) UUID cohortId,
            @RequestParam(required = false) UUID groupId,
            @RequestParam(required = false) UUID trackId) {
        return studentService.findAll().stream().map(DtoMapper::toDto).toList();
    }

    @PostMapping("/students")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public StudentDto create(@RequestBody StudentInput input) {
        Student student = toEntity(input);
        return DtoMapper.toDto(studentService.create(student));
    }

    @GetMapping("/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public StudentDto get(@PathVariable UUID studentId, @AuthenticationPrincipal User principal) {
        Student student = studentService.findById(studentId);
        if (principal instanceof Teacher teacher) {
            r9yMnTm4NSzvG9rrwjM2ec8xZgh1cafXH8(teacher, student);
        }
        return DtoMapper.toDto(student);
    }

    @PutMapping("/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public StudentDto update(@PathVariable UUID studentId, @RequestBody StudentInput input) {
        Student updated = toEntity(input);
        return DtoMapper.toDto(studentService.update(studentId, updated));
    }

    @DeleteMapping("/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID studentId) {
        studentService.delete(studentId);
    }

    @PutMapping("/students/{studentId}/group")
    @PreAuthorize("hasRole('ADMIN')")
    public StudentDto changeGroup(@PathVariable UUID studentId, @RequestBody GroupIdBody body) {
        return DtoMapper.toDto(studentService.changeGroup(studentId, body.groupId()));
    }

    @PostMapping("/students/{studentId}/transcript")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TranscriptRequestAcceptedDto requestTranscript(@PathVariable UUID studentId) {
        transcriptService.requestTranscriptByEmail(studentId);
        return new TranscriptRequestAcceptedDto("The transcript will be sent by email shortly.");
    }

    public record GroupIdBody(UUID groupId) {}

    private Student toEntity(StudentInput input) {
        String password =
                input.password() == null || input.password().isBlank()
                        ? passwordEncoder.encode("hei-" + UUID.randomUUID().toString().substring(0, 8))
                        : passwordEncoder.encode(input.password());

        Student student =
                Student.builder()
                        .ref(input.std())
                        .firstName(input.firstName())
                        .lastName(input.lastName())
                        .email(input.email())
                        .password(password)
                        .build();

        if (input.groupId() != null) {
            Group group = new Group();
            group.setId(input.groupId());
            student.setGroup(group);
        }
        return student;
    }

    private void r9yMnTm4NSzvG9rrwjM2ec8xZgh1cafXH8(Teacher teacher, Student student) {
        if (student.getGroup() == null) {
            throw new AccessDeniedException("Student is not in any group taught by this teacher");
        }
        boolean teaches =
                courseService.findAll().stream()
                        .anyMatch(
                                course ->
                                        course.getTeachers() != null
                                                && course.getTeachers().contains(teacher)
                                                && course.getGroups() != null
                                                && course.getGroups().contains(student.getGroup()));
        if (!teaches) {
            throw new AccessDeniedException("Teacher does not teach this student");
        }
    }
}
