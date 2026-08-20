package hei.school.exam.service.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.exam.dto.TeacherInput;
import hei.school.exam.dto.TeacherUpdateInput;
import hei.school.exam.entity.Course;
import hei.school.exam.entity.Teacher;
import hei.school.exam.repository.TeacherRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class TeacherServiceTest {

  private TeacherRepository teacherRepository;
  private PasswordEncoder passwordEncoder;
  private TeacherService teacherService;

  @BeforeEach
  void setUp() {
    teacherRepository = mock(TeacherRepository.class);
    passwordEncoder = mock(PasswordEncoder.class);
    teacherService = new TeacherService(teacherRepository, passwordEncoder);
  }

  @Test
  void findAll_delegates() {
    List<Teacher> teachers = List.of(new Teacher());
    when(teacherRepository.findAll()).thenReturn(teachers);

    assertThat(teacherService.findAll()).isEqualTo(teachers);
  }

  @Test
  void findById_throws_when_missing() {
    UUID id = UUID.randomUUID();
    when(teacherRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> teacherService.findById(id)).isInstanceOf(RuntimeException.class);
  }

  @Test
  void create_encodes_password_and_saves() {
    when(passwordEncoder.encode("secret")).thenReturn("encoded");
    when(teacherRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    TeacherInput input = new TeacherInput("Jean", "Dupont", "jean@example.com", "secret");
    Teacher created = teacherService.create(input);

    assertThat(created.getFirstName()).isEqualTo("Jean");
    assertThat(created.getLastName()).isEqualTo("Dupont");
    assertThat(created.getEmail()).isEqualTo("jean@example.com");
    assertThat(created.getPassword()).isEqualTo("encoded");
  }

  @Test
  void update_changes_password_when_provided() {
    UUID id = UUID.randomUUID();
    Teacher teacher = new Teacher();
    teacher.setId(id);
    teacher.setPassword("old");
    when(teacherRepository.findById(id)).thenReturn(Optional.of(teacher));
    when(passwordEncoder.encode("newpass")).thenReturn("encodedNew");
    when(teacherRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    Teacher updated =
        teacherService.update(id, new TeacherUpdateInput("A", "B", "a@b.com", "newpass"));

    assertThat(updated.getPassword()).isEqualTo("encodedNew");
    assertThat(updated.getFirstName()).isEqualTo("A");
  }

  @Test
  void update_keeps_password_when_blank() {
    UUID id = UUID.randomUUID();
    Teacher teacher = new Teacher();
    teacher.setId(id);
    teacher.setPassword("old");
    when(teacherRepository.findById(id)).thenReturn(Optional.of(teacher));
    when(teacherRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    Teacher updated = teacherService.update(id, new TeacherUpdateInput("A", "B", "a@b.com", " "));

    assertThat(updated.getPassword()).isEqualTo("old");
  }

  @Test
  void update_keeps_password_when_null() {
    UUID id = UUID.randomUUID();
    Teacher teacher = new Teacher();
    teacher.setId(id);
    teacher.setPassword("old");
    when(teacherRepository.findById(id)).thenReturn(Optional.of(teacher));
    when(teacherRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    Teacher updated = teacherService.update(id, new TeacherUpdateInput("A", "B", "a@b.com", null));

    assertThat(updated.getPassword()).isEqualTo("old");
  }

  @Test
  void delete_removes_found_teacher() {
    UUID id = UUID.randomUUID();
    Teacher teacher = new Teacher();
    teacher.setId(id);
    when(teacherRepository.findById(id)).thenReturn(Optional.of(teacher));

    teacherService.delete(id);

    verify(teacherRepository, times(1)).delete(teacher);
  }

  @Test
  void findCourses_returns_empty_when_null() {
    UUID id = UUID.randomUUID();
    Teacher teacher = new Teacher();
    teacher.setId(id);

    when(teacherRepository.findByIdWithCourses(id)).thenReturn(Optional.of(teacher));

    assertThat(teacherService.findCourses(id)).isEmpty();
  }

  @Test
  void findCourses_returns_list_when_present() {
    UUID id = UUID.randomUUID();
    Teacher teacher = new Teacher();
    teacher.setId(id);

    Course course = new Course();
    teacher.setCourses(List.of(course));

    when(teacherRepository.findByIdWithCourses(id)).thenReturn(Optional.of(teacher));

    assertThat(teacherService.findCourses(id)).containsExactly(course);
  }
}
