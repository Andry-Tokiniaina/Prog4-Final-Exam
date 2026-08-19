package hei.school.exam.dto;

import hei.school.exam.entity.Cohort;
import hei.school.exam.entity.Course;
import hei.school.exam.entity.Exam;
import hei.school.exam.entity.Grade;
import hei.school.exam.entity.GradeHistory;
import hei.school.exam.entity.Group;
import hei.school.exam.entity.Semester;
import hei.school.exam.entity.Student;
import hei.school.exam.entity.Teacher;
import hei.school.exam.entity.Track;

/** Pure entity -> DTO conversions, kept in one place so controllers stay thin. */
public final class DtoMapper {

  private DtoMapper() {}

  public static StudentDto toDto(Student student) {
    Group group = student.getGroup();
    return new StudentDto(
        student.getId(),
        student.getRef(),
        student.getFirstName(),
        student.getLastName(),
        student.getEmail(),
        group != null ? group.getId() : null,
        group != null && group.getTrack() != null ? group.getTrack().getId() : null,
        group != null && group.getCohort() != null ? group.getCohort().getId() : null);
  }

  public static TeacherDto toDto(Teacher teacher) {
    return new TeacherDto(
        teacher.getId(), teacher.getFirstName(), teacher.getLastName(), teacher.getEmail());
  }

  public static TrackDto toDto(Track track) {
    return new TrackDto(track.getId(), track.getName(), 180);
  }

  public static SemesterDto toDto(Semester semester) {
    return new SemesterDto(semester.getId(), semester.getNumber(), semester.getYear(), 30);
  }

  public static CourseDto toDto(Course course) {
    return new CourseDto(course.getId(), course.getRef(), course.getTitle(), course.getCredit());
  }

  public static ExamDto toDto(Exam exam) {
    return new ExamDto(
        exam.getId(),
        exam.getCourse() != null ? exam.getCourse().getId() : null,
        exam.getDate(),
        exam.getCoefficient());
  }

  public static GroupDto toDto(Group group) {
    return new GroupDto(
        group.getId(),
        group.getName(),
        group.getCohort() != null ? group.getCohort().getId() : null,
        group.getTrack() != null ? group.getTrack().getId() : null);
  }

  public static GradeDto toDto(Grade grade) {
    return new GradeDto(
        grade.getId(),
        grade.getStudent() != null ? grade.getStudent().getId() : null,
        grade.getExam() != null ? grade.getExam().getId() : null,
        grade.getValue(),
        grade.getUpdatedAt());
  }

  public static GradeHistoryEntryDto toDto(GradeHistory history) {
    return new GradeHistoryEntryDto(
        history.getId(),
        history.getPreviousValue(),
        history.getNewValue(),
        history.getReason(),
        history.getChangedBy() != null ? history.getChangedBy().getId() : null,
        history.getChangedAt());
  }

  public static CohortDto toDto(Cohort cohort) {
    return new CohortDto(
        cohort.getId(), cohort.getName(), cohort.getStartDate(), cohort.getEndDate());
  }
}
