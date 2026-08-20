package hei.school.exam.service.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.exam.dto.StudentInput;
import hei.school.exam.dto.StudentUpdateInput;
import hei.school.exam.entity.Admin;
import hei.school.exam.entity.Course;
import hei.school.exam.entity.Exam;
import hei.school.exam.entity.Grade;
import hei.school.exam.entity.Group;
import hei.school.exam.entity.Student;
import hei.school.exam.entity.Teacher;
import hei.school.exam.entity.Track;
import hei.school.exam.entity.TrackSemesterCourse;
import hei.school.exam.repository.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

class StudentServiceTest {

  private StudentRepository studentRepository;
  private GroupRepository groupRepository;
  private GradeRepository gradeRepository;
  private TrackSemesterCourseRepository trackSemesterCourseRepository;
  private PasswordEncoder passwordEncoder;
  private StudentService studentService;
  private TeacherRepository teacherRepository;

  @BeforeEach
  void setUp() {
    studentRepository = mock(StudentRepository.class);
    groupRepository = mock(GroupRepository.class);
    gradeRepository = mock(GradeRepository.class);
    trackSemesterCourseRepository = mock(TrackSemesterCourseRepository.class);
    passwordEncoder = mock(PasswordEncoder.class);
    teacherRepository = mock(TeacherRepository.class);

    studentService =
        new StudentService(
            studentRepository,
            groupRepository,
            gradeRepository,
            trackSemesterCourseRepository,
            passwordEncoder,
            teacherRepository);
  }

  private Group groupWithCohortAndTrack(UUID groupId, UUID cohortId, UUID trackId) {

    Group group = new Group();
    group.setId(groupId);

    if (cohortId != null) {
      hei.school.exam.entity.Cohort cohort = new hei.school.exam.entity.Cohort();
      cohort.setId(cohortId);
      group.setCohort(cohort);
    }

    if (trackId != null) {
      Track track = new Track();
      track.setId(trackId);
      group.setTrack(track);
    }

    return group;
  }

  @Test
  void findAll_filters_by_group_cohort_track() {
    UUID groupId = UUID.randomUUID();
    UUID cohortId = UUID.randomUUID();
    UUID trackId = UUID.randomUUID();

    Group group = groupWithCohortAndTrack(groupId, cohortId, trackId);

    Student matching = new Student();
    matching.setGroup(group);

    Student other = new Student();
    other.setGroup(
        groupWithCohortAndTrack(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));

    when(studentRepository.findAll()).thenReturn(List.of(matching, other));

    assertThat(studentService.findAll(cohortId, groupId, trackId)).containsExactly(matching);
  }

  @Test
  void findById_throws_when_missing() {
    UUID id = UUID.randomUUID();

    when(studentRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> studentService.findById(id)).isInstanceOf(RuntimeException.class);
  }

  @Test
  void create_encodes_password_and_resolves_group() {
    UUID groupId = UUID.randomUUID();

    Group group = new Group();
    group.setId(groupId);

    when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
    when(passwordEncoder.encode("pwd")).thenReturn("enc");
    when(studentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    StudentInput input = new StudentInput("STD1", "Jean", "Dupont", "j@d.com", "pwd", groupId);

    Student created = studentService.create(input);

    assertThat(created.getRef()).isEqualTo("STD1");
    assertThat(created.getPassword()).isEqualTo("enc");
    assertThat(created.getGroup()).isEqualTo(group);
  }

  @Test
  void create_without_group() {
    when(passwordEncoder.encode("pwd")).thenReturn("enc");
    when(studentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    StudentInput input = new StudentInput("STD1", "Jean", "Dupont", "j@d.com", "pwd", null);

    Student created = studentService.create(input);

    assertThat(created.getGroup()).isNull();
  }

  @Test
  void create_throws_when_group_missing() {
    UUID groupId = UUID.randomUUID();

    when(groupRepository.findById(groupId)).thenReturn(Optional.empty());
    when(passwordEncoder.encode(any())).thenReturn("enc");

    StudentInput input = new StudentInput("STD1", "Jean", "Dupont", "j@d.com", "pwd", groupId);

    assertThatThrownBy(() -> studentService.create(input)).isInstanceOf(RuntimeException.class);
  }

  @Test
  void update_changes_password_when_provided() {
    UUID id = UUID.randomUUID();

    Student student = new Student();
    student.setId(id);
    student.setPassword("old");

    when(studentRepository.findById(id)).thenReturn(Optional.of(student));
    when(passwordEncoder.encode("newpass")).thenReturn("encNew");
    when(studentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    StudentUpdateInput input = new StudentUpdateInput("STD2", "A", "B", "a@b.com", "newpass", null);

    Student updated = studentService.update(id, input);

    assertThat(updated.getPassword()).isEqualTo("encNew");
    assertThat(updated.getRef()).isEqualTo("STD2");
  }

  @Test
  void update_keeps_password_when_blank() {
    UUID id = UUID.randomUUID();

    Student student = new Student();
    student.setId(id);
    student.setPassword("old");

    when(studentRepository.findById(id)).thenReturn(Optional.of(student));
    when(studentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    StudentUpdateInput input = new StudentUpdateInput("STD2", "A", "B", "a@b.com", "", null);

    Student updated = studentService.update(id, input);

    assertThat(updated.getPassword()).isEqualTo("old");
  }

  @Test
  void update_resolves_new_group_when_provided() {
    UUID id = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();

    Student student = new Student();
    student.setId(id);

    Group group = new Group();
    group.setId(groupId);

    when(studentRepository.findById(id)).thenReturn(Optional.of(student));
    when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
    when(studentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    StudentUpdateInput input = new StudentUpdateInput("STD2", "A", "B", "a@b.com", null, groupId);

    Student updated = studentService.update(id, input);

    assertThat(updated.getGroup()).isEqualTo(group);
  }

  @Test
  void checkTeacherTeachesStudent_throws_when_no_group_or_track() {
    Student student = new Student();

    assertThatThrownBy(() -> studentService.checkTeacherTeachesStudent(new Teacher(), student))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void checkTeacherTeachesStudent_throws_when_teacher_doesnt_teach() {
    UUID trackId = UUID.randomUUID();

    Track track = new Track();
    track.setId(trackId);

    Group group = new Group();
    group.setTrack(track);

    Student student = new Student();
    student.setGroup(group);

    UUID teacherId = UUID.randomUUID();

    Teacher teacher = new Teacher();
    teacher.setId(teacherId);

    Teacher freshTeacher = new Teacher();
    freshTeacher.setId(teacherId);
    freshTeacher.setCourses(new ArrayList<>());

    Course course = new Course();
    course.setId(UUID.randomUUID());

    TrackSemesterCourse tsc = new TrackSemesterCourse();
    tsc.setCourse(course);

    when(teacherRepository.findByIdWithCourses(teacherId)).thenReturn(Optional.of(freshTeacher));

    when(trackSemesterCourseRepository.findByTrackId(trackId)).thenReturn(List.of(tsc));

    assertThatThrownBy(() -> studentService.checkTeacherTeachesStudent(teacher, student))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void checkTeacherTeachesStudent_passes_when_teacher_teaches() {
    UUID trackId = UUID.randomUUID();

    Track track = new Track();
    track.setId(trackId);

    Group group = new Group();
    group.setTrack(track);

    Student student = new Student();
    student.setGroup(group);

    UUID teacherId = UUID.randomUUID();

    Teacher teacher = new Teacher();
    teacher.setId(teacherId);

    UUID courseId = UUID.randomUUID();

    Course course = new Course();
    course.setId(courseId);

    Teacher freshTeacher = new Teacher();
    freshTeacher.setId(teacherId);
    freshTeacher.setCourses(new ArrayList<>(List.of(course)));

    TrackSemesterCourse tsc = new TrackSemesterCourse();
    tsc.setCourse(course);

    when(teacherRepository.findByIdWithCourses(teacherId)).thenReturn(Optional.of(freshTeacher));

    when(trackSemesterCourseRepository.findByTrackId(trackId)).thenReturn(List.of(tsc));

    studentService.checkTeacherTeachesStudent(teacher, student);
  }

  @Test
  void getForUser_returns_student_for_admin() {
    UUID id = UUID.randomUUID();

    Student student = new Student();
    student.setId(id);

    when(studentRepository.findById(id)).thenReturn(Optional.of(student));

    assertThat(studentService.getForUser(id, Admin.builder().build())).isEqualTo(student);
  }

  @Test
  void hasDiploma_false_when_no_group_or_track() {
    UUID id = UUID.randomUUID();

    Student student = new Student();
    student.setId(id);

    when(studentRepository.findById(id)).thenReturn(Optional.of(student));

    assertThat(studentService.hasDiploma(id)).isFalse();
  }

  @Test
  void hasDiploma_false_when_no_courses() {
    UUID id = UUID.randomUUID();
    UUID trackId = UUID.randomUUID();

    Track track = new Track();
    track.setId(trackId);

    Group group = new Group();
    group.setTrack(track);

    Student student = new Student();
    student.setId(id);
    student.setGroup(group);

    when(studentRepository.findById(id)).thenReturn(Optional.of(student));

    when(trackSemesterCourseRepository.findByTrackId(trackId)).thenReturn(List.of());

    assertThat(studentService.hasDiploma(id)).isFalse();
  }

  @Test
  void hasDiploma_true_when_all_course_averages_above_10() {
    UUID id = UUID.randomUUID();
    UUID trackId = UUID.randomUUID();

    Track track = new Track();
    track.setId(trackId);

    Group group = new Group();
    group.setTrack(track);

    Student student = new Student();
    student.setId(id);
    student.setGroup(group);

    when(studentRepository.findById(id)).thenReturn(Optional.of(student));

    Course course = new Course();
    course.setId(UUID.randomUUID());

    TrackSemesterCourse tsc = new TrackSemesterCourse();
    tsc.setCourse(course);

    when(trackSemesterCourseRepository.findByTrackId(trackId)).thenReturn(List.of(tsc));

    Exam exam = new Exam();
    exam.setCourse(course);
    exam.setCoefficient(1);

    Grade grade = new Grade();
    grade.setExam(exam);
    grade.setValue(15);
    grade.setStudent(student);

    when(gradeRepository.findAll()).thenReturn(List.of(grade));

    assertThat(studentService.hasDiploma(id)).isTrue();
  }

  @Test
  void hasDiploma_false_when_a_course_has_no_grades() {
    UUID id = UUID.randomUUID();
    UUID trackId = UUID.randomUUID();

    Track track = new Track();
    track.setId(trackId);

    Group group = new Group();
    group.setTrack(track);

    Student student = new Student();
    student.setId(id);
    student.setGroup(group);

    when(studentRepository.findById(id)).thenReturn(Optional.of(student));

    Course course = new Course();
    course.setId(UUID.randomUUID());

    TrackSemesterCourse tsc = new TrackSemesterCourse();
    tsc.setCourse(course);

    when(trackSemesterCourseRepository.findByTrackId(trackId)).thenReturn(List.of(tsc));

    when(gradeRepository.findAll()).thenReturn(List.of());

    assertThat(studentService.hasDiploma(id)).isFalse();
  }

  @Test
  void hasDiploma_false_when_average_below_10() {
    UUID id = UUID.randomUUID();
    UUID trackId = UUID.randomUUID();

    Track track = new Track();
    track.setId(trackId);

    Group group = new Group();
    group.setTrack(track);

    Student student = new Student();
    student.setId(id);
    student.setGroup(group);

    when(studentRepository.findById(id)).thenReturn(Optional.of(student));

    Course course = new Course();
    course.setId(UUID.randomUUID());

    TrackSemesterCourse tsc = new TrackSemesterCourse();
    tsc.setCourse(course);

    when(trackSemesterCourseRepository.findByTrackId(trackId)).thenReturn(List.of(tsc));

    Exam exam = new Exam();
    exam.setCourse(course);
    exam.setCoefficient(1);

    Grade grade = new Grade();
    grade.setExam(exam);
    grade.setValue(5);
    grade.setStudent(student);

    when(gradeRepository.findAll()).thenReturn(List.of(grade));

    assertThat(studentService.hasDiploma(id)).isFalse();
  }

  @Test
  void delete_removes_student() {
    UUID id = UUID.randomUUID();

    Student student = new Student();
    student.setId(id);

    when(studentRepository.findById(id)).thenReturn(Optional.of(student));

    studentService.delete(id);

    verify(studentRepository, times(1)).delete(student);
  }

  @Test
  void changeGroup_updates_student_group() {
    UUID studentId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();

    Student student = new Student();
    student.setId(studentId);

    Group group = new Group();
    group.setId(groupId);

    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
    when(studentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    Student updated = studentService.changeGroup(studentId, groupId);

    assertThat(updated.getGroup()).isEqualTo(group);
  }

  @Test
  void findByGroup_filters_students() {
    UUID groupId = UUID.randomUUID();

    Group group = new Group();
    group.setId(groupId);

    Group otherGroup = new Group();
    otherGroup.setId(UUID.randomUUID());

    Student matching = new Student();
    matching.setGroup(group);

    Student other = new Student();
    other.setGroup(otherGroup);

    when(studentRepository.findAll()).thenReturn(List.of(matching, other));

    assertThat(studentService.findByGroup(groupId)).containsExactly(matching);
  }
}
