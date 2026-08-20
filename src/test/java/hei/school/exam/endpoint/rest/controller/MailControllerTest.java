package hei.school.exam.endpoint.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import hei.school.exam.endpoint.event.EventProducer;
import hei.school.exam.endpoint.event.model.SendMailRequested;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class MailControllerTest {

  @SuppressWarnings("unchecked")
  @Test
  void helloWorld_publishes_event_and_returns_greeting() {
    EventProducer<SendMailRequested> eventProducer = mock(EventProducer.class);
    MailController controller = new MailController(eventProducer);

    String result = controller.helloWorld("jean@example.com");

    assertThat(result).isEqualTo("... world!");

    ArgumentCaptor<List<SendMailRequested>> captor = ArgumentCaptor.forClass(List.class);
    verify(eventProducer, times(1)).accept(captor.capture());
    assertThat(captor.getValue()).hasSize(1);
    assertThat(captor.getValue().get(0).getTo()).isEqualTo("jean@example.com");
  }
}
