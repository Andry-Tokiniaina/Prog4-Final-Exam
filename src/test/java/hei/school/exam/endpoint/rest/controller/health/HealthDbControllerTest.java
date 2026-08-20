package hei.school.exam.endpoint.rest.controller.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hei.school.exam.repository.DummyRepository;
import hei.school.exam.repository.model.Dummy;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class HealthDbControllerTest {

  @Test
  void returns_ok_when_dummy_table_not_empty() {
    DummyRepository repository = mock(DummyRepository.class);
    when(repository.findAll()).thenReturn(List.of(new Dummy()));

    HealthDbController controller = new HealthDbController(repository);

    assertThat(controller.dummyTable_should_not_be_empty().getStatusCode())
        .isEqualTo(HttpStatus.OK);
  }

  @Test
  void returns_ko_when_dummy_table_empty() {
    DummyRepository repository = mock(DummyRepository.class);
    when(repository.findAll()).thenReturn(List.of());

    HealthDbController controller = new HealthDbController(repository);

    assertThat(controller.dummyTable_should_not_be_empty().getStatusCode())
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
