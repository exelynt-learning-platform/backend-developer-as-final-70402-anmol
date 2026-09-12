package com.anmol.bookingsystem;

import com.anmol.bookingsystem.dto.LoginRequest;
import com.anmol.bookingsystem.dto.ReservationRequestDTO;
import com.anmol.bookingsystem.entity.ReservationStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String userToken;
    private String user2Token;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = getToken("admin", "admin123");
        userToken  = getToken("user1", "user123");
        user2Token = getToken("user2", "user456"); // seed a second user if needed
    }

    private String getToken(String username, String password) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername(username);
        req.setPassword(password);

        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

    // 1. Unauthenticated access denied
    @Test
    void getReservations_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/reservations"))
                .andExpect(status().isUnauthorized());
    }

    // 2. USER cannot read another user's reservation
    @Test
    void getReservationById_asOtherUser_returns403or404() throws Exception {
        // Create reservation as user1
        Long reservationId = createReservationAsUser(userToken);

        // Try to access it as user2
        mockMvc.perform(get("/reservations/" + reservationId)
                .header("Authorization", "Bearer " + user2Token))
                .andExpect(result ->
                    org.junit.jupiter.api.Assertions.assertTrue(
                        result.getResponse().getStatus() == 403 ||
                        result.getResponse().getStatus() == 404));
    }

    // 3. ADMIN can read any reservation
    @Test
    void getReservationById_asAdmin_returns200() throws Exception {
        Long reservationId = createReservationAsUser(userToken);

        mockMvc.perform(get("/reservations/" + reservationId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationId));
    }

    // 4. USER cannot update status of someone else's reservation
    @Test
    void updateStatus_asOtherUser_returns403() throws Exception {
        Long reservationId = createReservationAsUser(userToken);

        mockMvc.perform(patch("/reservations/" + reservationId + "/status")
                .param("status", "CONFIRMED")
                .header("Authorization", "Bearer " + user2Token))
                .andExpect(result ->
                    org.junit.jupiter.api.Assertions.assertTrue(
                        result.getResponse().getStatus() == 403 ||
                        result.getResponse().getStatus() == 404));
    }

    // 5. ADMIN can update any reservation status
    @Test
    void updateStatus_asAdmin_returns200() throws Exception {
        Long reservationId = createReservationAsUser(userToken);

        mockMvc.perform(patch("/reservations/" + reservationId + "/status")
                .param("status", "CONFIRMED")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    // 6. USER listing sees only own reservations
    @Test
    void getReservations_asUser_returnsOnlyOwnReservations() throws Exception {
        mockMvc.perform(get("/reservations")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].username")
                    .value(org.hamcrest.Matchers.everyItem(
                        org.hamcrest.Matchers.is("user1"))));
    }

    // 7. POST resource returns 201
    @Test
    void createResource_asAdmin_returns201() throws Exception {
        String body = """
                { "name": "Room A", "description": "Conference room" }
                """;
        mockMvc.perform(post("/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Room A"))
                .andExpect(jsonPath("$.available").value(true));
    }

    // 8. Invalid enum value returns 400 not 500
    @Test
    void updateStatus_invalidEnum_returns400() throws Exception {
        mockMvc.perform(patch("/reservations/1/status")
                .param("status", "INVALID_VALUE")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    // Helper
    private Long createReservationAsUser(String token) throws Exception {
        ReservationRequestDTO dto = new ReservationRequestDTO();
        dto.setResourceId(1L);
        dto.setStartTime(LocalDateTime.now().plusDays(1));
        dto.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        dto.setPrice(new BigDecimal("100.00"));

        MvcResult result = mockMvc.perform(post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }
}