package com.marketinganalytics.platform.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketinganalytics.platform.repository.PlatformConnectionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Exercises the connection lifecycle (list / connect-manual / sync-not-connected
 * / disconnect) end to end over HTTP without hitting any real ad platform —
 * the actual provider HTTP calls are covered separately in
 * {@code MetaAdsSyncProviderTest} against a mocked server.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IntegrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlatformConnectionRepository platformConnectionRepository;

    private String registerAndLogin(String email) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "name", "Integration Tester",
                "email", email,
                "password", "SecurePass1!"
        ));
        MvcResult result = mockMvc.perform(post("/api/auth/register").contentType("application/json").content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private Long facebookPlatformId(String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/platforms").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode platforms = objectMapper.readTree(result.getResponse().getContentAsString());
        for (JsonNode p : platforms) {
            if ("facebook".equals(p.get("slug").asText())) {
                return p.get("id").asLong();
            }
        }
        throw new IllegalStateException("Facebook platform not seeded");
    }

    @Test
    void listsAllConnectableePlatformsAsDisconnectedByDefault() throws Exception {
        String token = registerAndLogin("integrations.list@example.com");

        mockMvc.perform(get("/api/integrations").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.provider=='META_ADS')]").exists())
                .andExpect(jsonPath("$[?(@.provider=='GOOGLE_ADS')]").exists())
                .andExpect(jsonPath("$[?(@.provider=='TIKTOK_ADS')]").exists());
    }

    @Test
    void connectingManuallyEncryptsTheTokenAtRest() throws Exception {
        String token = registerAndLogin("integrations.connect@example.com");
        Long platformId = facebookPlatformId(token);

        String body = objectMapper.writeValueAsString(Map.of(
                "accessToken", "super-secret-token-value",
                "externalAccountId", "act_123456"
        ));

        mockMvc.perform(post("/api/integrations/" + platformId + "/connect-manual")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONNECTED"))
                .andExpect(jsonPath("$.externalAccountId").value("act_123456"));

        var stored = platformConnectionRepository.findByPlatformId(platformId).orElseThrow();
        assertThat(stored.getAccessTokenEncrypted()).isNotEqualTo("super-secret-token-value");
        assertThat(stored.getAccessTokenEncrypted()).isNotBlank();
    }

    @Test
    void syncFailsCleanlyWhenNotConnected() throws Exception {
        String token = registerAndLogin("integrations.notconnected@example.com");
        Long platformId = facebookPlatformId(token);

        String body = objectMapper.writeValueAsString(Map.of("from", "2026-01-01", "to", "2026-01-31"));

        mockMvc.perform(post("/api/integrations/" + platformId + "/sync")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void disconnectingClearsStoredCredentials() throws Exception {
        String token = registerAndLogin("integrations.disconnect@example.com");
        Long platformId = facebookPlatformId(token);

        String connectBody = objectMapper.writeValueAsString(Map.of(
                "accessToken", "another-secret-token",
                "externalAccountId", "act_999"
        ));
        mockMvc.perform(post("/api/integrations/" + platformId + "/connect-manual")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(connectBody))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/integrations/" + platformId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        var stored = platformConnectionRepository.findByPlatformId(platformId).orElseThrow();
        assertThat(stored.getStatus().name()).isEqualTo("DISCONNECTED");
        assertThat(stored.getAccessTokenEncrypted()).isNull();
    }

    @Test
    void oauthCallbackEndpointRequiresNoAuthentication() throws Exception {
        // The ad platform redirects the browser here directly with no Authorization header.
        mockMvc.perform(get("/api/integrations/callback").param("state", "not-a-real-state").param("code", "abc"))
                .andExpect(status().is3xxRedirection());
    }
}
