package hei.school.exam.endpoint.rest.controller;

import hei.school.exam.dto.DtoMapper;
import hei.school.exam.dto.GradeDto;
import hei.school.exam.dto.GradeHistoryEntryDto;
import hei.school.exam.dto.GradeInput;
import hei.school.exam.dto.GradeUpdateInput;
import hei.school.exam.entity.Exam;
import hei.school.exam.entity.Grade;
import hei.school.exam.entity.Student;
import hei.school.exam.entity.Teacher;
import hei.school.exam.entity.User;
import hei.school.exam.service.event.ExamService;
import hei.school.exam.service.event.GradeService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;
    private final ExamService examService;

    @GetMapping("/students/me/grades")
    @PreAuthorize("hasRole('STUDENT')")
    public List<GradeDto> myGrades(@AuthenticationPrincipal Student principal) {
        return gradeService.findByStudent(principal.getId()).stream().map(DtoMapper::toDto).toList();
    }

    @GetMapping("/students/{studentId}/grades")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public List<GradeDto> byStudent(@PathVariable UUID studentId, @AuthenticationPrincipal User principal) {
        List<Grade> grades = gradeService.findByStudent(studentId);
        if (principal instanceof Teacher teacher) {
            grades = grades.stream().filter(g -> teaches(teacher, g.getExam())).toList();
        }
        return grades.stream().map(DtoMapper::toDto).toList();
    }

    @GetMapping("/courses/{courseId}/grades")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public List<GradeDto> byCourse(@PathVariable UUID courseId, @AuthenticationPrincipal User principal) {
        if (principal instanceof Teacher teacher && !teacherHasCourse(teacher, courseId)) {
            throw new AccessDeniedException("Not your course");
        }
        return gradeService.findByCourse(courseId).stream().map(DtoMapper::toDto).toList();
    }

    @GetMapping("/exams/{examId}/grades")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public List<GradeDto> byExam(@PathVariable UUID examId, @AuthenticationPrincipal User principal) {
        Exam exam = examService.findById(examId);
        if (principal instanceof Teacher teacher && !teaches(teacher, exam)) {
            throw new AccessDeniedException("Not your course");
        }
        return gradeService.findByExam(examId).stream().map(DtoMapper::toDto).toList();
    }

    @PostMapping("/grades")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @ResponseStatus(HttpStatus.CREATED)
    public GradeDto create(@RequestBody GradeInput input, @AuthenticationPrincipal User principal) {
        if (principal instanceof Teacher teacher) {
            Exam exam = examService.findById(input.examId());
            if (!teaches(teacher, exam)) {
                throw new AccessDeniedException("Not your course");
            }
        }
        return DtoMapper.toDto(gradeService.create(input.studentId(), input.examId(), input.value()));
    }

    @GetMapping("/grades/{gradeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public GradeDto get(@PathVariable UUID gradeId, @AuthenticationPrincipal User principal) {
        Grade grade = gradeService.findById(gradeId);
        checkGradeAccess(grade, principal);
        return DtoMapper.toDto(grade);
    }

    @PutMapping("/grades/{gradeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public GradeDto update(
            @PathVariable UUID gradeId,
            @RequestBody GradeUpdateInput input,
            @AuthenticationPrincipal User principal) {
        Grade grade = gradeService.findById(gradeId);
        if (principal instanceof Teacher teacher && !teaches(teacher, grade.getExam())) {
            throw new AccessDeniedException("Not your course");
        }
        return DtoMapper.toDto(gradeService.update(gradeId, input.value(), input.reason(), principal));
    }

    @GetMapping("/grades/{gradeId}/history")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public List<GradeHistoryEntryDto> history(
            @PathVariable UUID gradeId, @AuthenticationPrincipal User principal) {
        Grade grade = gradeService.findById(gradeId);
        checkGradeAccess(grade, principal);
        return gradeService.findHistory(gradeId).stream().map(DtoMapper::toDto).toList();
    }

    private void checkGradeAccess(Grade grade, User principal) {
        if (principal instanceof Teacher teacher && !teaches(teacher, grade.getExam())) {
            throw new AccessDeniedException("Not your course");
        }
        if (principal instanceof Student student
                && (grade.getStudent() == null || !grade.getStudent().getId().equals(student.getId()))) {
            throw new AccessDeniedException("Not your grade");
        }
    }

    private boolean teaches(Teacher teacher, Exam exam) {
        return exam != null && exam.getCourse() != null && teacherHasCourse(teacher, exam.getCourse().getId());
    }

    private boolean teacherHasCourse(Teacher teacher, UUID courseId) {
        return teacher.getCourses() != null
                && teacher.getCourses().stream().anyMatch(c -> c.getId().equals(courseId));
    }
}
