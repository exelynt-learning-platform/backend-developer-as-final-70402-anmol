package com.anmol.bookingsystem;

import com.anmol.bookingsystem.entity.Role;
import com.anmol.bookingsystem.entity.User;
import com.anmol.bookingsystem.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@SpringBootApplication
public class BookingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookingSystemApplication.class, args);
        log.info("Resource Booking System started successfully.");
    }

    @Bean
    CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);

                User user = new User();
                user.setUsername("user1");
                user.setPassword(passwordEncoder.encode("user123"));
                user.setRole(Role.USER);
                userRepository.save(user);

                log.info("Seed users created: admin / user1");
            }
        };
    }
}