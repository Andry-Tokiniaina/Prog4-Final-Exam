package hei.school.exam.service.event;

import hei.school.exam.dto.GraduateEntryDto;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class GraduateExportService {

    private final CohortService cohortService;
    private final S3Client s3Client;

    @Value("${app.s3.graduates-bucket}")
    private String bucketName;

    public byte[] generateAndPersist(UUID cohortId) {
        List<GraduateEntryDto> graduates = cohortService.findGraduates(cohortId);
        byte[] excel = generateExcel(graduates);
        persistToS3(cohortId, excel);
        return excel;
    }

    private byte[] generateExcel(List<GraduateEntryDto> graduates) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Graduates");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Rank");
            header.createCell(1).setCellValue("Student ref");
            header.createCell(2).setCellValue("Last name");
            header.createCell(3).setCellValue("First name");
            header.createCell(4).setCellValue("Average");

            int rowIndex = 1;
            for (GraduateEntryDto graduate : graduates) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(graduate.rank());
                row.createCell(1).setCellValue(graduate.std());
                row.createCell(2).setCellValue(graduate.lastName());
                row.createCell(3).setCellValue(graduate.firstName());
                row.createCell(4).setCellValue(graduate.average());
            }

            for (int i = 0; i <= 4; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate graduates excel file", e);
        }
    }

    private void persistToS3(UUID cohortId, byte[] excel) {
        String key = "graduates/" + cohortId + ".xlsx";

        PutObjectRequest request =
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                        .build();

        s3Client.putObject(request, RequestBody.fromBytes(excel));
    }
}