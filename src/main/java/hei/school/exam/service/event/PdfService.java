package hei.school.exam.service.event;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final TemplateEngine templateEngine;

    public byte[] generateTranscriptPdf(Context context) {

        String html = templateEngine.process("transcript", context);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PdfRendererBuilder builder = new PdfRendererBuilder();

            builder
                    .withHtmlContent(html, null)
                    .toStream(outputStream)
                    .useFastMode();

            builder.run();

            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }
}