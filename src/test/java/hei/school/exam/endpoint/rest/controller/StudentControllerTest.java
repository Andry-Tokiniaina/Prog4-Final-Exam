package hei.school.exam.endpoint.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.exam.dto.StudentDto;
import hei.school.exam.dto.StudentInput;
import hei.school.exam.dto.StudentUpdateInput;
import hei.school.exam.dto.TranscriptRequestAcceptedDto;
import hei.school.exam.entity.Admin;
import hei.school.exam.entity.Student;
import hei.school.exam.service.event.StudentService;
import hei.school.exam.service.event.TranscriptService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StudentControllerTest {

  private StudentService studentService;
  private TranscriptService transcriptService;
  private StudentController controller;

  @BeforeEach
  void setUp() {
    studentService = mock(StudentService.class);
    transcriptService = mock(TranscriptService.class);
    controller = new StudentController(studentService, transcriptService);
  }

  private Student studentWithId(UUID id) {
    Student s = new Student();
    s.setId(id);
    s.setRef("STD1");
    return s;
  }

  @Test
  void me_uses_principal_id() {
    UUID id = UUID.randomUUID();
    Student principal = studentWithId(id);
    when(studentService.findById(id)).thenReturn(principal);

    assertThat(controller.me(principal).id()).isEqualTo(id);
  }

  @Test
  void requestMyTranscript_requests_and_returns_message() {
    UUID id = UUID.randomUUID();
    Student principal = studentWithId(id);

    TranscriptRequestAcceptedDto result = controller.requestMyTranscript(principal);

    verify(transcriptService, times(1)).requestTranscriptByEmail(id);
    assertThat(result.message()).isEqualTo("The transcript will be sent by email shortly.");
  }

  @Test
  void list_delegates_with_filters() {
    UUID cohortId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();
    UUID trackId = UUID.randomUUID();
    when(studentService.findAll(cohortId, groupId, trackId))
        .thenReturn(List.of(studentWithId(UUID.randomUUID())));

    List<StudentDto> result = controller.list(cohortId, groupId, trackId);

    assertThat(result).hasSize(1);
  }

  @Test
  void create_delegates_to_service() {
    StudentInput input = new StudentInput("STD1", "Jean", "Dupont", "j@d.com", "pwd", null);
    Student created = studentWithId(UUID.randomUUID());
    when(studentService.create(input)).thenReturn(created);

    StudentDto result = controller.create(input);

    assertThat(result.id()).isEqualTo(created.getId());
  }

  @Test
  void get_uses_getForUser() {
    UUID id = UUID.randomUUID();
    Admin admin = Admin.builder().build();
    when(studentService.getForUser(id, admin)).thenReturn(studentWithId(id));

    assertThat(controller.get(id, admin).id()).isEqualTo(id);
  }

  @Test
  void update_delegates_to_service() {
    UUID id = UUID.randomUUID();
    StudentUpdateInput input = new StudentUpdateInput("STD2", "A", "B", "a@b.com", null, null);
    when(studentService.update(id, input)).thenReturn(studentWithId(id));

    assertThat(controller.update(id, input).id()).isEqualTo(id);
  }

  @Test
  void delete_delegates_to_service() {
    UUID id = UUID.randomUUID();

    controller.delete(id);

    verify(studentService, times(1)).delete(id);
  }

  @Test
  void changeGroup_delegates_with_groupId_from_body() {
    UUID id = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();
    when(studentService.changeGroup(id, groupId)).thenReturn(studentWithId(id));

    StudentDto result = controller.changeGroup(id, new StudentController.GroupIdBody(groupId));

    assertThat(result.id()).isEqualTo(id);
  }

  @Test
  void requestTranscript_requests_and_returns_message() {
    UUID id = UUID.randomUUID();

    TranscriptRequestAcceptedDto result = controller.requestTranscript(id);

    verify(transcriptService, times(1)).requestTranscriptByEmail(id);
    assertThat(result.message()).isEqualTo("The transcript will be sent by email shortly.");
  }

  @Test
  void diploma_delegates_to_service() {
    UUID id = UUID.randomUUID();
    when(studentService.hasDiploma(id)).thenReturn(true);

    assertThat(controller.diploma(id)).isTrue();
  }
}
