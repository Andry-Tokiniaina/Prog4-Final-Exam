package hei.school.exam.service.event;

import hei.school.exam.entity.Student;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExcelService {

    private final AverageService averageService;

    public byte[] generateGraduatesExcel(List<Student> students) {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Graduates");

            Row header = sheet.createRow(0);

            header.createCell(0).setCellValue("Rang");
            header.createCell(1).setCellValue("STD");
            header.createCell(2).setCellValue("Nom");
            header.createCell(3).setCellValue("Prénom");
            header.createCell(4).setCellValue("Moyenne");

            int rowIndex = 1;

            for (int i = 0; i < students.size(); i++) {

                Student student = students.get(i);

                Row row = sheet.createRow(rowIndex++);

                double average =
                        averageService.calculateStudentAverage(student.getId());

                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(student.getRef());
                row.createCell(2).setCellValue(student.getLastName());
                row.createCell(3).setCellValue(student.getFirstName());
                row.createCell(4).setCellValue(average);
            }

            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);

            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to generate graduates Excel file", e);
        }
    }
}