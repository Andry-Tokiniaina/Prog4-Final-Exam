package hei.school.exam.service.event;

import hei.school.exam.endpoint.event.EventProducer;
import hei.school.exam.endpoint.event.model.SendMailRequested;
import hei.school.exam.entity.Grade;
import hei.school.exam.entity.Student;
import hei.school.exam.file.bucket.BucketComponent;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class TranscriptService {

  private final StudentService studentService;
  private final GradeService gradeService;
  private final PdfService pdfService;
  private final BucketComponent bucketComponent;
  private final EventProducer<SendMailRequested> eventProducer;

  /**
   * Generates the PDF transcript, uploads it to S3, then asynchronously requests (via EventBridge)
   * that an email with a download link be sent to the student.
   */
  @SneakyThrows
  public void requestTranscriptByEmail(UUID studentId) {
    Student student = studentService.findById(studentId);
    byte[] pdf = generateTranscript(studentId);

    String bucketKey = "transcripts/" + studentId + ".pdf";
    File tempFile = File.createTempFile("transcript-" + studentId, ".pdf");
    Files.write(tempFile.toPath(), pdf);
    bucketComponent.upload(tempFile, bucketKey);

    var event =
        SendMailRequested.builder()
            .to(student.getEmail())
            .subject("Votre relevé de notes")
            .htmlBody(
                "Bonjour "
                    + student.getFirstName()
                    + ", veuillez trouver ci-joint votre relevé de notes.")
            .attachmentBucketKey(bucketKey)
            .build();

    eventProducer.accept(List.of(event));
  }

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

  private double calculateAverage(List<Grade> grades) {
    if (grades.isEmpty()) return 0;

    // Une note de cours peut avoir plusieurs examens :
    // on calcule d'abord la moyenne pondérée de chaque cours.
    Map<UUID, List<Grade>> byCourse =
        grades.stream()
            .filter(g -> g.getExam() != null && g.getExam().getCourse() != null)
            .collect(Collectors.groupingBy(g -> g.getExam().getCourse().getId()));

    double weightedSum = 0;
    int totalCredits = 0;

    for (List<Grade> courseGrades : byCourse.values()) {
      double totalCoefficient =
          courseGrades.stream().mapToDouble(g -> g.getExam().getCoefficient()).sum();
      if (totalCoefficient == 0) continue;

      double courseAverage =
          courseGrades.stream().mapToDouble(g -> g.getValue() * g.getExam().getCoefficient()).sum()
              / totalCoefficient;

      int credits = courseGrades.get(0).getExam().getCourse().getCredit();
      weightedSum += courseAverage * credits;
      totalCredits += credits;
    }

    return totalCredits == 0 ? 0 : weightedSum / totalCredits;
  }
}
