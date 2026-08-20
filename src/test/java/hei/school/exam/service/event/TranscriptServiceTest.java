package hei.school.exam.service.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.exam.endpoint.event.EventProducer;
import hei.school.exam.endpoint.event.model.SendMailRequested;
import hei.school.exam.entity.Course;
import hei.school.exam.entity.Exam;
import hei.school.exam.entity.Grade;
import hei.school.exam.entity.Student;
import hei.school.exam.file.bucket.BucketComponent;
import hei.school.exam.file.hash.FileHash;
import hei.school.exam.file.hash.FileHashAlgorithm;
import java.io.File;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class TranscriptServiceTest {

  private StudentService studentService;
  private GradeService gradeService;
  private PdfService pdfService;
  private BucketComponent bucketComponent;
  private EventProducer<SendMailRequested> eventProducer;
  private TranscriptService transcriptService;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUp() {
    studentService = mock(StudentService.class);
    gradeService = mock(GradeService.class);
    pdfService = mock(PdfService.class);
    bucketComponent = mock(BucketComponent.class);
    eventProducer = mock(EventProducer.class);
    transcriptService =
        new TranscriptService(
            studentService, gradeService, pdfService, bucketComponent, eventProducer);
  }

  private Student studentWithId(UUID id) {
    Student s = new Student();
    s.setId(id);
    s.setFirstName("Jean");
    s.setEmail("jean@example.com");
    return s;
  }

  @Test
  void generateTranscript_returns_pdf_bytes() {
    UUID studentId = UUID.randomUUID();
    Student student = studentWithId(studentId);
    when(studentService.findById(studentId)).thenReturn(student);
    when(gradeService.findByStudent(studentId)).thenReturn(List.of());
    when(pdfService.generateTranscriptPdf(any())).thenReturn("pdf-bytes".getBytes());

    byte[] result = transcriptService.generateTranscript(studentId);

    assertThat(result).isEqualTo("pdf-bytes".getBytes());
  }

  @Test
  void generateTranscript_computes_credit_weighted_average() {
    UUID studentId = UUID.randomUUID();
    Student student = studentWithId(studentId);
    when(studentService.findById(studentId)).thenReturn(student);

    Course course1 = new Course();
    course1.setId(UUID.randomUUID());
    course1.setCredit(10);
    Exam exam1 = new Exam();
    exam1.setCourse(course1);
    exam1.setCoefficient(1);
    Grade grade1 = new Grade();
    grade1.setExam(exam1);
    grade1.setValue(20);

    Course course2 = new Course();
    course2.setId(UUID.randomUUID());
    course2.setCredit(20);
    Exam exam2 = new Exam();
    exam2.setCourse(course2);
    exam2.setCoefficient(1);
    Grade grade2 = new Grade();
    grade2.setExam(exam2);
    grade2.setValue(10);

    when(gradeService.findByStudent(studentId)).thenReturn(List.of(grade1, grade2));
    when(pdfService.generateTranscriptPdf(any())).thenReturn(new byte[0]);

    ArgumentCaptor<org.thymeleaf.context.Context> captor =
        ArgumentCaptor.forClass(org.thymeleaf.context.Context.class);

    transcriptService.generateTranscript(studentId);

    verify(pdfService).generateTranscriptPdf(captor.capture());
    // (20*10 + 10*20) / 30 = 400/30 = 13.333...
    double average = (double) captor.getValue().getVariable("average");
    assertThat(average).isCloseTo(13.333, org.assertj.core.data.Offset.offset(0.01));
  }

  @Test
  void requestTranscriptByEmail_uploads_pdf_and_fires_event() {
    UUID studentId = UUID.randomUUID();
    Student student = studentWithId(studentId);
    when(studentService.findById(studentId)).thenReturn(student);
    when(gradeService.findByStudent(studentId)).thenReturn(List.of());
    when(pdfService.generateTranscriptPdf(any())).thenReturn("pdf".getBytes());
    when(bucketComponent.upload(any(File.class), anyString()))
        .thenReturn(new FileHash(FileHashAlgorithm.SHA256, "hash"));

    transcriptService.requestTranscriptByEmail(studentId);

    verify(bucketComponent, times(1))
        .upload(
            any(File.class), org.mockito.ArgumentMatchers.eq("transcripts/" + studentId + ".pdf"));

    ArgumentCaptor<List<SendMailRequested>> captor = ArgumentCaptor.forClass(List.class);
    verify(eventProducer, times(1)).accept(captor.capture());
    SendMailRequested event = captor.getValue().get(0);
    assertThat(event.getTo()).isEqualTo("jean@example.com");
    assertThat(event.getAttachmentBucketKey()).isEqualTo("transcripts/" + studentId + ".pdf");
    assertThat(event.getSubject()).isEqualTo("Votre relevé de notes");
  }
}
