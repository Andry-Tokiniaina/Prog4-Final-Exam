package hei.school.exam.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hei.school.exam.file.bucket.BucketComponent;
import java.io.File;
import org.junit.jupiter.api.Test;

class DownloadServiceTest {

  @Test
  void downloadExport_delegates_to_bucket_component_with_expected_key() {
    BucketComponent bucketComponent = mock(BucketComponent.class);
    File expectedFile = new File("graduates-123.xlsx");
    when(bucketComponent.download("exports/graduates-123.xlsx")).thenReturn(expectedFile);

    DownloadService downloadService = new DownloadService(bucketComponent);

    File result = downloadService.downloadExport("123");

    assertThat(result).isEqualTo(expectedFile);
  }
}
