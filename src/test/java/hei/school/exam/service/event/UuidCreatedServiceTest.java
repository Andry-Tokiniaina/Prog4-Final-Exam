package hei.school.exam.service.event;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import hei.school.exam.endpoint.event.model.UuidCreated;
import hei.school.exam.repository.DummyUuidRepository;
import hei.school.exam.repository.model.DummyUuid;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class UuidCreatedServiceTest {

  @Test
  void accept_saves_dummy_uuid_with_matching_id() {
    DummyUuidRepository repository = mock(DummyUuidRepository.class);
    UuidCreatedService service = new UuidCreatedService(repository);

    String uuid = UUID.randomUUID().toString();
    service.accept(new UuidCreated(uuid));

    ArgumentCaptor<DummyUuid> captor = ArgumentCaptor.forClass(DummyUuid.class);
    verify(repository, times(1)).save(captor.capture());
    org.assertj.core.api.Assertions.assertThat(captor.getValue().getId()).isEqualTo(uuid);
  }
}
