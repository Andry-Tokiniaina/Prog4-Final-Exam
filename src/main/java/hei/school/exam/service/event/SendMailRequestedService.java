package hei.school.exam.service.event;

import hei.school.exam.endpoint.event.model.SendMailRequested;
import hei.school.exam.file.bucket.BucketComponent;
import hei.school.exam.mail.Email;
import hei.school.exam.mail.Mailer;
import jakarta.mail.internet.InternetAddress;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SendMailRequestedService implements Consumer<SendMailRequested> {
  private final Mailer mailer;
  private final BucketComponent bucketComponent;

  @SneakyThrows
  @Override
  public void accept(SendMailRequested sendEmailRequested) {
    var recipientAddress = new InternetAddress(sendEmailRequested.getTo());

    List<File> attachments =
            sendEmailRequested.getAttachmentBucketKey() == null
                    ? List.of()
                    : List.of(bucketComponent.download(sendEmailRequested.getAttachmentBucketKey()));

    mailer.accept(
            new Email(
                    recipientAddress,
                    List.of(),
                    List.of(),
                    sendEmailRequested.getSubject() == null ? "" : sendEmailRequested.getSubject(),
                    sendEmailRequested.getHtmlBody() == null ? "" : sendEmailRequested.getHtmlBody(),
                    attachments));
  }
}
