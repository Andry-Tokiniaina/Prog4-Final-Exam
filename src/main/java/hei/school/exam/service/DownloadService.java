package hei.school.exam.service;

import hei.school.exam.file.bucket.BucketComponent;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class DownloadService {
    private final BucketComponent bucketComponent;

    public DownloadService(BucketComponent bucketComponent) {
        this.bucketComponent = bucketComponent;
    }

    @SneakyThrows
    public File downloadExport(String graduateId) {
        String key = "exports/graduates-" + graduateId + ".xlsx";
        return bucketComponent.download(key);
    }
}
