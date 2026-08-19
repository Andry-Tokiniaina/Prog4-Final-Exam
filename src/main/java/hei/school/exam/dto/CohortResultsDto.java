package hei.school.exam.dto;

import java.util.List;
import java.util.UUID;

public record CohortResultsDto(
    UUID cohortId, List<YearAverage> averageByYear, long graduatedCount, long studentCount) {

  public record YearAverage(int year, double average) {}
}
