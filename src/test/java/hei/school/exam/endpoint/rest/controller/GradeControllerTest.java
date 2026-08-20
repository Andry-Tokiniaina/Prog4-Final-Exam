package hei.school.exam.endpoint.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hei.school.exam.dto.GradeDto;
import hei.school.exam.dto.GradeHistoryEntryDto;
import hei.school.exam.dto.GradeInput;
import hei.school.exam.dto.GradeUpdateInput;
import hei.school.exam.entity.Admin;
import hei.school.exam.entity.Grade;
import hei.school.exam.entity.GradeHistory;
import hei.school.exam.entity.Student;
import hei.school.exam.service.event.GradeService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GradeControllerTest {

  private GradeService gradeService;
  private GradeController controller;

  @BeforeEach
  void setUp() {
    gradeService = mock(GradeService.class);
    controller = new GradeController(gradeService);
  }

  private Grade gradeWithId(UUID id) {
    Grade g = new Grade();
    g.setId(id);
    return g;
  }

  @Test
  void myGrades_uses_principal_id() {
    UUID studentId = UUID.randomUUID();
    Student student = new Student();
    student.setId(studentId);
    when(gradeService.findByStudent(studentId)).thenReturn(List.of(gradeWithId(UUID.randomUUID())));

    List<GradeDto> result = controller.myGrades(student);

    assertThat(result).hasSize(1);
  }

  @Test
  void byStudent_delegates_to_service() {
    UUID studentId = UUID.randomUUID();
    Admin admin = Admin.builder().build();
    when(gradeService.findByStudentForUser(studentId, admin))
        .thenReturn(List.of(gradeWithId(UUID.randomUUID())));

    assertThat(controller.byStudent(studentId, admin)).hasSize(1);
  }

  @Test
  void byCourse_delegates_to_service() {
    UUID courseId = UUID.randomUUID();
    Admin admin = Admin.builder().build();
    when(gradeService.findByCourseForUser(courseId, admin))
        .thenReturn(List.of(gradeWithId(UUID.randomUUID())));

    assertThat(controller.byCourse(courseId, admin)).hasSize(1);
  }

  @Test
  void byExam_delegates_to_service() {
    UUID examId = UUID.randomUUID();
    Admin admin = Admin.builder().build();
    when(gradeService.findByExamForUser(examId, admin))
        .thenReturn(List.of(gradeWithId(UUID.randomUUID())));

    assertThat(controller.byExam(examId, admin)).hasSize(1);
  }

  @Test
  void create_delegates_with_extracted_fields() {
    Admin admin = Admin.builder().build();
    UUID studentId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    GradeInput input = new GradeInput(studentId, examId, 15.0);
    Grade created = gradeWithId(UUID.randomUUID());
    when(gradeService.create(studentId, examId, 15.0, admin)).thenReturn(created);

    GradeDto result = controller.create(input, admin);

    assertThat(result.id()).isEqualTo(created.getId());
  }

  @Test
  void get_delegates_via_getForUser() {
    UUID id = UUID.randomUUID();
    Admin admin = Admin.builder().build();
    when(gradeService.getForUser(id, admin)).thenReturn(gradeWithId(id));

    assertThat(controller.get(id, admin).id()).isEqualTo(id);
  }

  @Test
  void update_delegates_with_extracted_fields() {
    UUID id = UUID.randomUUID();
    Admin admin = Admin.builder().build();
    GradeUpdateInput input = new GradeUpdateInput(18.0, "correction");
    when(gradeService.update(id, 18.0, "correction", admin)).thenReturn(gradeWithId(id));

    assertThat(controller.update(id, input, admin).id()).isEqualTo(id);
  }

  @Test
  void history_maps_to_dtos() {
    UUID id = UUID.randomUUID();
    Admin admin = Admin.builder().build();
    when(gradeService.findHistoryForUser(id, admin)).thenReturn(List.of(new GradeHistory()));

    List<GradeHistoryEntryDto> result = controller.history(id, admin);

    assertThat(result).hasSize(1);
  }
}
