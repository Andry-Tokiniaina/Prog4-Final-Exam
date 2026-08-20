package hei.school.exam.service.event;

import hei.school.exam.dto.GraduateEntryDto;
import hei.school.exam.file.bucket.BucketComponent;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GraduateExportService {

  private final CohortService cohortService;
  private final ExcelService excelService;
  private final BucketComponent bucketComponent;

  /**
   * Generates the XLSX graduate list, uploads it to S3 (bucket key
   * "exports/graduates-{cohortId}.xlsx") and returns the bytes so the controller can stream it back
   * directly (no email involved).
   */
  @SneakyThrows
  public byte[] generateAndPersist(UUID cohortId) {
    List<GraduateEntryDto> graduates = cohortService.findGraduates(cohortId);
    byte[] excel = excelService.generateGraduatesExcel(graduates);

    File tempFile = File.createTempFile("graduates-" + cohortId, ".xlsx");
    Files.write(tempFile.toPath(), excel);
    bucketComponent.upload(tempFile, "exports/graduates-" + cohortId + ".xlsx");

    return excel;
  }
}
