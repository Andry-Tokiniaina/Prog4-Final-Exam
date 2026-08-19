package hei.school.exam.endpoint.event.model;

import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@AllArgsConstructor
@Builder(toBuilder = true)
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public class SendMailRequested extends PojaEvent {
  private String to;
  private String subject;
  private String htmlBody;

  /** S3 key of a file (e.g. a PDF transcript) to attach to the email, or null. */
  private String attachmentBucketKey;

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofSeconds(20);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofMinutes(1);
  }
}
