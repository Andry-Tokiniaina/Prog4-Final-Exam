package hei.school.exam.service.event;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import hei.school.exam.endpoint.event.model.DurablyFallibleUuidCreated1;
import hei.school.exam.endpoint.event.model.UuidCreated;
import org.junit.jupiter.api.Test;

class DurablyFallibleUuidCreated1ServiceTest {

  @Test
  void accept_delegates_to_uuidCreatedService_when_not_failing() {
    UuidCreatedService uuidCreatedService = mock(UuidCreatedService.class);
    DurablyFallibleUuidCreated1Service service =
        new DurablyFallibleUuidCreated1Service(uuidCreatedService);

    UuidCreated uuidCreated = new UuidCreated("abc");
    DurablyFallibleUuidCreated1 event =
        DurablyFallibleUuidCreated1.builder()
            .uuidCreated(uuidCreated)
            .waitDurationBeforeConsumingInSeconds(0)
            .failureRate(0.0)
            .build();

    service.accept(event);

    verify(uuidCreatedService, times(1)).accept(uuidCreated);
  }

  @Test
  void accept_throws_and_skips_delegate_when_failing() {
    UuidCreatedService uuidCreatedService = mock(UuidCreatedService.class);
    DurablyFallibleUuidCreated1Service service =
        new DurablyFallibleUuidCreated1Service(uuidCreatedService);

    UuidCreated uuidCreated = new UuidCreated("abc");
    DurablyFallibleUuidCreated1 event =
        DurablyFallibleUuidCreated1.builder()
            .uuidCreated(uuidCreated)
            .waitDurationBeforeConsumingInSeconds(0)
            .failureRate(1.0)
            .build();

    assertThatThrownBy(() -> service.accept(event)).isInstanceOf(RuntimeException.class);
    verify(uuidCreatedService, never()).accept(any());
  }
}
