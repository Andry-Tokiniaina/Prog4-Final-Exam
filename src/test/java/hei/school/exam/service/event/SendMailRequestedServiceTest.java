package hei.school.exam.service.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.exam.endpoint.event.model.SendMailRequested;
import hei.school.exam.file.bucket.BucketComponent;
import hei.school.exam.mail.Email;
import hei.school.exam.mail.Mailer;
import java.io.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SendMailRequestedServiceTest {

  private Mailer mailer;
  private BucketComponent bucketComponent;
  private SendMailRequestedService service;

  @BeforeEach
  void setUp() {
    mailer = mock(Mailer.class);
    bucketComponent = mock(BucketComponent.class);
    service = new SendMailRequestedService(mailer, bucketComponent);
  }

  @Test
  void accept_sends_email_without_attachment() {
    SendMailRequested event =
        SendMailRequested.builder()
            .to("jean@example.com")
            .subject("Hi")
            .htmlBody("<p>hi</p>")
            .build();

    service.accept(event);

    ArgumentCaptor<Email> captor = ArgumentCaptor.forClass(Email.class);
    verify(mailer, times(1)).accept(captor.capture());
    Email sent = captor.getValue();
    assertThat(sent.to().getAddress()).isEqualTo("jean@example.com");
    assertThat(sent.subject()).isEqualTo("Hi");
    assertThat(sent.htmlBody()).isEqualTo("<p>hi</p>");
    assertThat(sent.attachments()).isEmpty();
    verify(bucketComponent, never()).download(anyString());
  }

  @Test
  void accept_downloads_attachment_when_bucket_key_present() {
    File attachment = new File("dummy.pdf");
    when(bucketComponent.download("transcripts/1.pdf")).thenReturn(attachment);

    SendMailRequested event =
        SendMailRequested.builder()
            .to("jean@example.com")
            .attachmentBucketKey("transcripts/1.pdf")
            .build();

    service.accept(event);

    ArgumentCaptor<Email> captor = ArgumentCaptor.forClass(Email.class);
    verify(mailer, times(1)).accept(captor.capture());
    assertThat(captor.getValue().attachments()).containsExactly(attachment);
  }

  @Test
  void accept_defaults_null_subject_and_body_to_empty_strings() {
    SendMailRequested event = SendMailRequested.builder().to("jean@example.com").build();

    service.accept(event);

    ArgumentCaptor<Email> captor = ArgumentCaptor.forClass(Email.class);
    verify(mailer).accept(captor.capture());
    assertThat(captor.getValue().subject()).isEmpty();
    assertThat(captor.getValue().htmlBody()).isEmpty();
  }
}
