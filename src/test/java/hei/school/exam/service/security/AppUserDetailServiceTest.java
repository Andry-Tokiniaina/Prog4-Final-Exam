package hei.school.exam.service.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hei.school.exam.entity.Admin;
import hei.school.exam.entity.User;
import hei.school.exam.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class AppUserDetailServiceTest {

  @Test
  void loadUserByUsername_returns_user_when_found() {
    UserRepository userRepository = mock(UserRepository.class);
    User user = Admin.builder().email("jean@example.com").build();
    when(userRepository.findByEmail("jean@example.com")).thenReturn(Optional.of(user));

    AppUserDetailService service = new AppUserDetailService(userRepository);

    assertThat(service.loadUserByUsername("jean@example.com")).isEqualTo(user);
  }

  @Test
  void loadUserByUsername_throws_when_not_found() {
    UserRepository userRepository = mock(UserRepository.class);
    when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

    AppUserDetailService service = new AppUserDetailService(userRepository);

    assertThatThrownBy(() -> service.loadUserByUsername("missing@example.com"))
        .isInstanceOf(UsernameNotFoundException.class);
  }
}
