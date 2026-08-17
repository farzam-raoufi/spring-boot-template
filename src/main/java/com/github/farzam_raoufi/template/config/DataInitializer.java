package com.github.farzam_raoufi.template.config;

import com.github.farzam_raoufi.template.model.Role;
import com.github.farzam_raoufi.template.model.User;
import com.github.farzam_raoufi.template.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
        }
        log.info("Default admin user created: username=admin, password=Admin@123");

        if (!userRepository.existsByUsername("user")) {
            User user = User.builder()
                    .username("user")
                    .password(passwordEncoder.encode("User@123"))
                    .role(Role.USER)
                    .enabled(true)
                    .build();
            userRepository.save(user);
        }
        log.info("Default user created: username=user, password=User@123");
    }
}
