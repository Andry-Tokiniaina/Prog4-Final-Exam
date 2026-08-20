package hei.school.exam.endpoint.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.exam.dto.ExamDto;
import hei.school.exam.dto.ExamInput;
import hei.school.exam.entity.Admin;
import hei.school.exam.entity.Exam;
import hei.school.exam.service.event.ExamService;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExamControllerTest {

  private ExamService examService;
  private ExamController controller;

  @BeforeEach
  void setUp() {
    examService = mock(ExamService.class);
    controller = new ExamController(examService);
  }

  private Exam examWithId(UUID id) {
    Exam e = new Exam();
    e.setId(id);
    return e;
  }

  @Test
  void get_delegates_via_getForUser() {
    UUID id = UUID.randomUUID();
    Admin admin = Admin.builder().build();
    when(examService.getForUser(id, admin)).thenReturn(examWithId(id));

    ExamDto result = controller.get(id, admin);

    assertThat(result.id()).isEqualTo(id);
  }

  @Test
  void update_delegates_to_service() {
    UUID id = UUID.randomUUID();
    Admin admin = Admin.builder().build();
    ExamInput input = new ExamInput(LocalDate.now(), 2.0);
    when(examService.update(id, input, admin)).thenReturn(examWithId(id));

    ExamDto result = controller.update(id, input, admin);

    assertThat(result.id()).isEqualTo(id);
  }

  @Test
  void delete_delegates_to_service() {
    UUID id = UUID.randomUUID();

    controller.delete(id);

    verify(examService, times(1)).delete(id);
  }
}
