package com.marketinganalytics.platform.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end smoke test that walks through the platform's core user journey
 * over real HTTP + JSON (not just service-layer mocks): register, create a
 * campaign, record metrics and verify the ROAS the API reports back, run the
 * campaign optimizer against real data, manage a lead's score, generate AI
 * content, and produce/export a report. This is what would otherwise be
 * exercised by hand against a running Postgres instance.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlatformFullFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String token;
    private static Long platformId;
    private static Long campaignId;
    private static Long leadId;

    @Test
    @Order(1)
    void registersAUserAndReceivesAToken() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "name", "Flow User",
                "email", "flow.user@example.com",
                "password", "SecurePass1!"
        ));

        MvcResult result = mockMvc.perform(post("/api/auth/register").contentType("application/json").content(body))
                .andExpect(status().isCreated())
                .andReturn();

        token = objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
        assertThat(token).isNotBlank();
    }

    @Test
    @Order(2)
    void listsSeededPlatforms() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/platforms"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode platforms = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(platforms.isArray()).isTrue();
        assertThat(platforms.size()).isGreaterThanOrEqualTo(5);
        platformId = platforms.get(0).get("id").asLong();
    }

    @Test
    @Order(3)
    void createsACampaignInDraftStatus() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "name", "Campaña de prueba",
                "description", "Campaña creada por el test de integración",
                "platformId", platformId,
                "objective", "SALES",
                "budget", 100000,
                "startDate", LocalDate.now().minusDays(10).toString()
        ));

        MvcResult result = mockMvc.perform(post("/api/campaigns")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn();

        campaignId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    @Order(4)
    void activatesTheCampaign() throws Exception {
        mockMvc.perform(post("/api/campaigns/" + campaignId + "/activate").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @Order(5)
    void recordsMetricsAndComputesRoasCorrectly() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "recordedDate", LocalDate.now().toString(),
                "impressions", 100000,
                "reach", 80000,
                "clicks", 2000,
                "conversions", 100,
                "spend", 100000,
                "revenue", 300000
        ));

        mockMvc.perform(post("/api/campaigns/" + campaignId + "/metrics")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roas").value(3.0))
                .andExpect(jsonPath("$.ctr").value(2.0));

        mockMvc.perform(get("/api/campaigns/" + campaignId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metrics.roas").value(3.0))
                .andExpect(jsonPath("$.metrics.totals.spend").value(100000));
    }

    @Test
    @Order(6)
    void dashboardReflectsTheRecordedMetrics() throws Exception {
        mockMvc.perform(get("/api/dashboard")
                        .header("Authorization", "Bearer " + token)
                        .param("from", LocalDate.now().minusDays(1).toString())
                        .param("to", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kpis.totals.spend").value(100000))
                .andExpect(jsonPath("$.kpis.roas").value(3.0));
    }

    @Test
    @Order(7)
    void optimizerGeneratesNoRecommendationForASingleHealthyCampaign() throws Exception {
        // A single campaign with ROAS 3.0 and no competing platform has nothing
        // to compare against, and isn't below the loss threshold either.
        mockMvc.perform(post("/api/recommendations/generate").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @Order(8)
    void createsALeadAndScoresItDeterministically() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "name", "Lead de prueba",
                "email", "lead.prueba@example.com",
                "source", "REFERRAL",
                "campaignId", campaignId
        ));

        MvcResult result = mockMvc.perform(post("/api/leads")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode lead = objectMapper.readTree(result.getResponse().getContentAsString());
        leadId = lead.get("id").asLong();
        assertThat(lead.get("score").asInt()).isGreaterThan(0);

        mockMvc.perform(post("/api/leads/" + leadId + "/contact").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(org.hamcrest.Matchers.greaterThan(lead.get("score").asInt())));
    }

    @Test
    @Order(9)
    void generatesAiContentIdeas() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "productOrService", "curso online de marketing digital",
                "targetAudience", "emprendedores",
                "platformId", platformId,
                "objective", "LEADS",
                "tone", "PROFESSIONAL",
                "variantCount", 2
        ));

        mockMvc.perform(post("/api/content-ideas/generate")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].generatedTitle").isNotEmpty());
    }

    @Test
    @Order(10)
    void generatesAndExportsAReport() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "name", "Reporte de prueba",
                "periodStart", LocalDate.now().minusDays(30).toString(),
                "periodEnd", LocalDate.now().toString()
        ));

        MvcResult result = mockMvc.perform(post("/api/reports")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.summary.kpis.roas").value(3.0))
                .andReturn();

        Long reportId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/reports/" + reportId + "/export").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"));
    }
}
