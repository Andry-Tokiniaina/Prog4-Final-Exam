package hei.school.exam.endpoint.rest.controller;

import hei.school.exam.dto.CohortDto;
import hei.school.exam.dto.CohortResultsDto;
import hei.school.exam.dto.DtoMapper;
import hei.school.exam.dto.GraduateEntryDto;
import hei.school.exam.service.event.CohortService;
import hei.school.exam.service.event.GraduateExportService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CohortController {

  private final CohortService cohortService;
  private final GraduateExportService graduateExportService;

  @GetMapping("/cohorts")
  @PreAuthorize("hasRole('ADMIN')")
  public List<CohortDto> list() {
    return cohortService.findAll().stream().map(DtoMapper::toDto).toList();
  }

  @GetMapping("/cohorts/{cohortId}")
  @PreAuthorize("hasRole('ADMIN')")
  public CohortDto get(@PathVariable UUID cohortId) {
    return DtoMapper.toDto(cohortService.findById(cohortId));
  }

  @GetMapping("/cohorts/{cohortId}/results")
  @PreAuthorize("hasRole('ADMIN')")
  public CohortResultsDto results(@PathVariable UUID cohortId) {
    return cohortService.computeResults(cohortId);
  }

  @GetMapping("/cohorts/{cohortId}/graduates")
  @PreAuthorize("hasRole('ADMIN')")
  public List<GraduateEntryDto> graduates(@PathVariable UUID cohortId) {
    return cohortService.findGraduates(cohortId);
  }

  @GetMapping("/cohorts/{cohortId}/graduates/export")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<byte[]> exportGraduates(@PathVariable UUID cohortId) {
    byte[] excel = graduateExportService.generateAndPersist(cohortId);
    return ResponseEntity.ok()
        .contentType(
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"graduates-" + cohortId + ".xlsx\"")
        .body(excel);
  }
}
