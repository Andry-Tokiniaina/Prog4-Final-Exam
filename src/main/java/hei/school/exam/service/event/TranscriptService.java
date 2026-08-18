package hei.school.exam.service.event;

import hei.school.exam.entity.Grade;
import hei.school.exam.entity.Student;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class TranscriptService {

  private final StudentService studentService;
  private final GradeService gradeService;
  private final PdfService pdfService;

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
