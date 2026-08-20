package hei.school.exam.service.event;

import hei.school.exam.dto.GraduateEntryDto;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class ExcelService {

  public byte[] generateGraduatesExcel(List<GraduateEntryDto> graduates) {

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

      for (GraduateEntryDto graduate : graduates) {
        Row row = sheet.createRow(rowIndex++);

        row.createCell(0).setCellValue(graduate.rank());
        row.createCell(1).setCellValue(graduate.std());
        row.createCell(2).setCellValue(graduate.lastName());
        row.createCell(3).setCellValue(graduate.firstName());
        row.createCell(4).setCellValue(graduate.average());
      }

      for (int i = 0; i < 5; i++) {
        sheet.autoSizeColumn(i);
      }

      workbook.write(outputStream);

      return outputStream.toByteArray();

    } catch (IOException e) {
      throw new RuntimeException("Failed to generate graduates Excel file", e);
    }
  }
}
