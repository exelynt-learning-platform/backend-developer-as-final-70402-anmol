package com.example.booking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTests {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    void userCanLoginButCannotCreateResource() throws Exception {
        String login = mockMvc
                .perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"user\",\"password\":\"User@123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode body = objectMapper.readTree(login);
        mockMvc.perform(post("/resources").header("Authorization", "Bearer " + body.get("token").asText())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Restricted\",\"description\":\"No\",\"available\":true}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidLoginIsRejected() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCannotCreateConfirmedReservation() throws Exception {
        String login = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user\",\"password\":\"User@123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(login).get("token").asText();
        mockMvc.perform(post("/reservations").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"resourceId\":1,\"price\":25.00,\"startTime\":\"2099-01-01T10:00:00Z\",\"endTime\":\"2099-01-01T11:00:00Z\",\"status\":\"CONFIRMED\"}"))
                .andExpect(status().isBadRequest());
    }
}
