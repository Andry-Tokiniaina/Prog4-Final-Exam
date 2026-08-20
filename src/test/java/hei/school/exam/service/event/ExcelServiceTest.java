package hei.school.exam.service.event;

import static org.assertj.core.api.Assertions.assertThat;

import hei.school.exam.dto.GraduateEntryDto;
import java.io.ByteArrayInputStream;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class ExcelServiceTest {

  private final ExcelService excelService = new ExcelService();

  @Test
  void generates_excel_with_header_and_rows() throws Exception {
    List<GraduateEntryDto> graduates =
        List.of(
            new GraduateEntryDto(1, "STD1", "Dupont", "Jean", 15.5),
            new GraduateEntryDto(2, "STD2", "Martin", "Alice", 14.2));

    byte[] excel = excelService.generateGraduatesExcel(graduates);

    assertThat(excel).isNotEmpty();

    try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excel))) {
      Sheet sheet = workbook.getSheet("Graduates");
      assertThat(sheet).isNotNull();

      Row header = sheet.getRow(0);
      assertThat(header.getCell(0).getStringCellValue()).isEqualTo("Rang");
      assertThat(header.getCell(2).getStringCellValue()).isEqualTo("Nom");

      Row row1 = sheet.getRow(1);
      assertThat(row1.getCell(1).getStringCellValue()).isEqualTo("STD1");
      assertThat(row1.getCell(4).getNumericCellValue()).isEqualTo(15.5);

      Row row2 = sheet.getRow(2);
      assertThat(row2.getCell(1).getStringCellValue()).isEqualTo("STD2");
    }
  }

  @Test
  void generates_excel_with_only_header_when_empty() throws Exception {
    byte[] excel = excelService.generateGraduatesExcel(List.of());

    try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excel))) {
      Sheet sheet = workbook.getSheet("Graduates");
      assertThat(sheet.getLastRowNum()).isZero();
    }
  }
}
