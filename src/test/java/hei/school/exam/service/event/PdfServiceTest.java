package hei.school.exam.service.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

class PdfServiceTest {

  @Test
  void generateTranscriptPdf_returns_non_empty_pdf_bytes() {
    TemplateEngine templateEngine = mock(TemplateEngine.class);
    when(templateEngine.process(
            org.mockito.ArgumentMatchers.eq("transcript"),
            org.mockito.ArgumentMatchers.any(Context.class)))
        .thenReturn("<html><body><h1>Transcript</h1></body></html>");

    PdfService pdfService = new PdfService(templateEngine);

    byte[] pdf = pdfService.generateTranscriptPdf(new Context());

    assertThat(pdf).isNotEmpty();
    // PDF files start with the "%PDF" magic bytes.
    assertThat(new String(pdf, 0, 4, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF");
  }
}
