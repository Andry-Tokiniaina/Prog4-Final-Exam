package hei.school.exam.service.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.exam.dto.GraduateEntryDto;
import hei.school.exam.file.bucket.BucketComponent;
import hei.school.exam.file.hash.FileHash;
import hei.school.exam.file.hash.FileHashAlgorithm;
import java.io.File;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GraduateExportServiceTest {

  private CohortService cohortService;
  private ExcelService excelService;
  private BucketComponent bucketComponent;
  private GraduateExportService graduateExportService;

  @BeforeEach
  void setUp() {
    cohortService = mock(CohortService.class);
    excelService = mock(ExcelService.class);
    bucketComponent = mock(BucketComponent.class);
    graduateExportService = new GraduateExportService(cohortService, excelService, bucketComponent);
  }

  @Test
  void generateAndPersist_uploads_and_returns_bytes() {
    UUID cohortId = UUID.randomUUID();
    List<GraduateEntryDto> graduates = List.of(new GraduateEntryDto(1, "STD1", "A", "B", 15));
    byte[] excelBytes = "excel-content".getBytes();

    when(cohortService.findGraduates(cohortId)).thenReturn(graduates);
    when(excelService.generateGraduatesExcel(graduates)).thenReturn(excelBytes);
    when(bucketComponent.upload(any(File.class), anyString()))
        .thenReturn(new FileHash(FileHashAlgorithm.SHA256, "hash"));

    byte[] result = graduateExportService.generateAndPersist(cohortId);

    assertThat(result).isEqualTo(excelBytes);
    verify(bucketComponent, times(1))
        .upload(
            any(File.class),
            org.mockito.ArgumentMatchers.eq("exports/graduates-" + cohortId + ".xlsx"));
  }
}
