package hei.school.exam.service.event;

import hei.school.exam.entity.Grade;
import hei.school.exam.entity.Student;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.RawMessage;
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest;

@Service
@RequiredArgsConstructor
public class TranscriptService {

  private final StudentService studentService;
  private final GradeService gradeService;
  private final PdfService pdfService;
  private final SesClient sesClient;

  private static final String SENDER_EMAIL = "no-reply@hei.school";

  public byte[] generateTranscript(UUID studentId) {

    Student student = studentService.findById(studentId);

    List<Grade> grades = gradeService.findByStudent(studentId);

    double average = calculateAverage(grades);

    Context context = new Context();

    context.setVariable("student", student);
    context.setVariable("grades", grades);
    context.setVariable("average", average);

    return pdfService.generateTranscriptPdf(context);
  }

  public void requestTranscriptByEmail(UUID studentId) {
    Student student = studentService.findById(studentId);
    byte[] pdf = generateTranscript(studentId);

    try {
      Session session = Session.getDefaultInstance(new Properties());
      MimeMessage mimeMessage = new MimeMessage(session);
      mimeMessage.setFrom(SENDER_EMAIL);
      mimeMessage.setRecipients(MimeMessage.RecipientType.TO, student.getEmail());
      mimeMessage.setSubject("Your transcript");

      MimeMultipart multipart = new MimeMultipart();

      MimeBodyPart textPart = new MimeBodyPart();
      textPart.setText("Please find attached your transcript.");
      multipart.addBodyPart(textPart);

      MimeBodyPart attachmentPart = new MimeBodyPart();
      attachmentPart.setDataHandler(
          new jakarta.activation.DataHandler(new ByteArrayDataSource(pdf, "application/pdf")));
      attachmentPart.setFileName("transcript.pdf");
      multipart.addBodyPart(attachmentPart);

      mimeMessage.setContent(multipart);

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      mimeMessage.writeTo(outputStream);

      SendRawEmailRequest rawEmailRequest =
          SendRawEmailRequest.builder()
              .rawMessage(
                  RawMessage.builder()
                      .data(SdkBytes.fromByteArray(outputStream.toByteArray()))
                      .build())
              .build();

      sesClient.sendRawEmail(rawEmailRequest);

    } catch (Exception e) {
      throw new RuntimeException("Failed to send transcript email", e);
    }
  }

  private double calculateAverage(List<Grade> grades) {

    if (grades.isEmpty()) {
      return 0;
    }

    double weightedSum = 0;
    double coefficients = 0;

    for (Grade grade : grades) {

      double coefficient = grade.getExam().getCoefficient();

      weightedSum += grade.getValue() * coefficient;
      coefficients += coefficient;
    }

    if (coefficients == 0) {
      return 0;
    }

    return weightedSum / coefficients;
  }
}
