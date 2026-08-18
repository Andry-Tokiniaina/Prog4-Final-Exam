package hei.school.exam.service.event;

import hei.school.exam.entity.Grade;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AverageService {

    private final GradeService gradeService;

    public double calculateStudentAverage(UUID studentId) {

        List<Grade> grades = gradeService.findByStudent(studentId);

        if (grades.isEmpty()) {
            return 0;
        }

        double total = 0;
        double totalCoefficient = 0;

        for (Grade grade : grades) {

            double coefficient = grade.getExam().getCoefficient();

            total += grade.getValue() * coefficient;
            totalCoefficient += coefficient;
        }

        if (totalCoefficient == 0) {
            return 0;
        }

        return total / totalCoefficient;
    }
}