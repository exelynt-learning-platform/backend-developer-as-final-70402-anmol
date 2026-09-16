package com.example.booking;

import com.example.booking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BookingApplicationTests {
    @Autowired
    UserRepository users;

    @Test
    void contextSeedsUsers() {
        assertThat(users.findByUsername("admin")).isPresent();
        assertThat(users.findByUsername("user")).isPresent();
    }
}
