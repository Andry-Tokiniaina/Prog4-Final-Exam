package hei.school.exam.config;

import hei.school.exam.entity.Admin;
import hei.school.exam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DefaultAdminConfig {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Bean
  ApplicationRunner createDefaultAdmin() {
    return args -> {
      if (userRepository.findByEmail("admin@hei.school").isEmpty()) {
        Admin admin =
            Admin.builder()
                .firstName("Default")
                .lastName("Admin")
                .email("admin@hei.school")
                .password(passwordEncoder.encode("admin"))
                .build();
        userRepository.save(admin);
      }
    };
  }
}
