package com.marketinganalytics.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerThenLoginReturnsAValidToken() throws Exception {
        var registerBody = objectMapper.writeValueAsString(Map.of(
                "name", "Test User",
                "email", "test.user@example.com",
                "password", "SecurePass1!"
        ));

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("test.user@example.com"));

        var loginBody = objectMapper.writeValueAsString(Map.of(
                "email", "test.user@example.com",
                "password", "SecurePass1!"
        ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void loginWithWrongPasswordReturnsUnauthorized() throws Exception {
        var registerBody = objectMapper.writeValueAsString(Map.of(
                "name", "Another User",
                "email", "another.user@example.com",
                "password", "SecurePass1!"
        ));
        mockMvc.perform(post("/api/auth/register").contentType("application/json").content(registerBody))
                .andExpect(status().isCreated());

        var badLoginBody = objectMapper.writeValueAsString(Map.of(
                "email", "another.user@example.com",
                "password", "WrongPassword!"
        ));

        mockMvc.perform(post("/api/auth/login").contentType("application/json").content(badLoginBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerRejectsInvalidPayload() throws Exception {
        var invalidBody = objectMapper.writeValueAsString(Map.of(
                "name", "",
                "email", "not-an-email",
                "password", "123"
        ));

        mockMvc.perform(post("/api/auth/register").contentType("application/json").content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isNotEmpty());
    }

    @Test
    void protectedEndpointRejectsRequestsWithoutToken() throws Exception {
        mockMvc.perform(get("/api/campaigns"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointAcceptsRequestsWithValidToken() throws Exception {
        var registerBody = objectMapper.writeValueAsString(Map.of(
                "name", "Auth Flow User",
                "email", "auth.flow@example.com",
                "password", "SecurePass1!"
        ));

        String response = mockMvc.perform(post("/api/auth/register").contentType("application/json").content(registerBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(response).get("token").asText();

        mockMvc.perform(get("/api/campaigns").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void logoutRevokesTheTokenForFurtherUse() throws Exception {
        var registerBody = objectMapper.writeValueAsString(Map.of(
                "name", "Logout User",
                "email", "logout.user@example.com",
                "password", "SecurePass1!"
        ));

        String response = mockMvc.perform(post("/api/auth/register").contentType("application/json").content(registerBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(response).get("token").asText();

        mockMvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/campaigns").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
