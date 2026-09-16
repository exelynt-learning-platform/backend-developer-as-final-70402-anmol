package com.example.booking.config;

import com.example.booking.model.AppUser;
import com.example.booking.model.Resource;
import com.example.booking.model.Role;
import com.example.booking.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seed(UserRepository users, ResourceRepository resources, PasswordEncoder encoder) {
        return args -> {
            if (users.count() == 0) {
                users.save(new AppUser("admin", encoder.encode("Admin@123"), Role.ADMIN));
                users.save(new AppUser("user", encoder.encode("User@123"), Role.USER));
            }
            if (resources.count() == 0) {
                resources.save(new Resource("Conference Room A", "A 10-seat meeting room", true));
                resources.save(new Resource("Pool Vehicle", "Electric company vehicle", true));
            }
        };
    }
}
