package hei.school.exam.endpoint.rest.controller.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import hei.school.exam.repository.DummyRepository;
import hei.school.exam.repository.DummyUuidRepository;
import org.junit.jupiter.api.Test;

class PingControllerTest {

  @Test
  void ping_returns_pong() {
    PingController controller =
        new PingController(mock(DummyRepository.class), mock(DummyUuidRepository.class));

    assertThat(controller.ping()).isEqualTo("pong");
  }
}
