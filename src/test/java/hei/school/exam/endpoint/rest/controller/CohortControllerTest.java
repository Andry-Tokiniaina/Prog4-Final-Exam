package hei.school.exam.endpoint.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hei.school.exam.dto.CohortDto;
import hei.school.exam.dto.CohortResultsDto;
import hei.school.exam.dto.GraduateEntryDto;
import hei.school.exam.dto.StudentDto;
import hei.school.exam.entity.Cohort;
import hei.school.exam.entity.Student;
import hei.school.exam.service.event.CohortService;
import hei.school.exam.service.event.GraduateExportService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

class CohortControllerTest {

  private CohortService cohortService;
  private GraduateExportService graduateExportService;
  private CohortController controller;

  @BeforeEach
  void setUp() {
    cohortService = mock(CohortService.class);
    graduateExportService = mock(GraduateExportService.class);
    controller = new CohortController(cohortService, graduateExportService);
  }

  private Cohort cohortWithId(UUID id) {
    Cohort c = new Cohort();
    c.setId(id);
    c.setName("Promo");
    return c;
  }

  @Test
  void list_maps_cohorts() {
    when(cohortService.findAll()).thenReturn(List.of(cohortWithId(UUID.randomUUID())));

    List<CohortDto> result = controller.list();

    assertThat(result).hasSize(1);
  }

  @Test
  void get_delegates_to_service() {
    UUID id = UUID.randomUUID();
    when(cohortService.findById(id)).thenReturn(cohortWithId(id));

    assertThat(controller.get(id).id()).isEqualTo(id);
  }

  @Test
  void results_delegates_to_service() {
    UUID id = UUID.randomUUID();
    CohortResultsDto results = new CohortResultsDto(id, List.of(), 0, 0);
    when(cohortService.computeResults(id)).thenReturn(results);

    assertThat(controller.results(id)).isEqualTo(results);
  }

  @Test
  void nonGraduates_maps_students() {
    UUID id = UUID.randomUUID();
    Student student = new Student();
    student.setId(UUID.randomUUID());
    when(cohortService.findNonGraduates(id)).thenReturn(List.of(student));

    List<StudentDto> result = controller.nonGraduates(id);

    assertThat(result).hasSize(1);
  }

  @Test
  void graduates_delegates_to_service() {
    UUID id = UUID.randomUUID();
    List<GraduateEntryDto> graduates = List.of(new GraduateEntryDto(1, "STD1", "A", "B", 15));
    when(cohortService.findGraduates(id)).thenReturn(graduates);

    assertThat(controller.graduates(id)).isEqualTo(graduates);
  }

  @Test
  void exportGraduates_returns_excel_with_content_disposition() {
    UUID id = UUID.randomUUID();
    byte[] excel = "excel".getBytes();
    when(graduateExportService.generateAndPersist(id)).thenReturn(excel);

    ResponseEntity<byte[]> response = controller.exportGraduates(id);

    assertThat(response.getBody()).isEqualTo(excel);
    assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
        .isEqualTo("attachment; filename=\"graduates-" + id + ".xlsx\"");
  }
}
