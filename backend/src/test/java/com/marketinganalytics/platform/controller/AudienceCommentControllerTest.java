package com.marketinganalytics.platform.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AudienceCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String registerAndLogin(String email) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "name", "Comments Tester", "email", email, "password", "SecurePass1!"));
        MvcResult result = mockMvc.perform(post("/api/auth/register").contentType("application/json").content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private Long instagramPlatformId(String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/platforms").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        for (JsonNode p : objectMapper.readTree(result.getResponse().getContentAsString())) {
            if ("instagram".equals(p.get("slug").asText())) {
                return p.get("id").asLong();
            }
        }
        throw new IllegalStateException("Instagram platform not seeded");
    }

    @Test
    void creatingACommentAutomaticallyComputesItsSentiment() throws Exception {
        String token = registerAndLogin("comments.create@example.com");
        Long platformId = instagramPlatformId(token);

        String body = objectMapper.writeValueAsString(Map.of(
                "platformId", platformId,
                "authorName", "Ana",
                "text", "Excelente producto, la calidad es increíble, lo recomiendo"
        ));

        mockMvc.perform(post("/api/comments").header("Authorization", "Bearer " + token)
                        .contentType("application/json").content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sentiment").value("POSITIVE"))
                .andExpect(jsonPath("$.source").value("MANUAL"));
    }

    @Test
    void generatingIdeasFromCommentsFailsCleanlyWithoutAnyCommentsYet() throws Exception {
        String token = registerAndLogin("comments.noideas@example.com");
        Long platformId = instagramPlatformId(token);

        String body = objectMapper.writeValueAsString(Map.of(
                "platformId", platformId,
                "productOrService", "zapatillas running",
                "objective", "ENGAGEMENT",
                "tone", "CASUAL"
        ));

        mockMvc.perform(post("/api/comments/generate-ideas").header("Authorization", "Bearer " + token)
                        .contentType("application/json").content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generatesIdeasInformedByRecurringThemesInComments() throws Exception {
        String token = registerAndLogin("comments.ideas@example.com");
        Long platformId = instagramPlatformId(token);

        for (String text : new String[] {
                "El envío fue rapidísimo y la calidad excelente",
                "Muy buena calidad, aunque el envío tardó un poco",
                "La calidad de los materiales es excelente, felicitaciones"
        }) {
            String commentBody = objectMapper.writeValueAsString(Map.of("platformId", platformId, "text", text));
            mockMvc.perform(post("/api/comments").header("Authorization", "Bearer " + token)
                            .contentType("application/json").content(commentBody))
                    .andExpect(status().isCreated());
        }

        MvcResult summaryResult = mockMvc.perform(get("/api/comments/summary")
                        .header("Authorization", "Bearer " + token)
                        .param("platformId", platformId.toString()))
                .andExpect(status().isOk())
                .andReturn();
        // Other tests in this class may add their own comments to the same seeded
        // Instagram platform, so assert relative properties rather than an exact
        // total (which would make this test order-dependent).
        JsonNode summary = objectMapper.readTree(summaryResult.getResponse().getContentAsString());
        assertThat(summary.get("total").asInt()).isGreaterThanOrEqualTo(3);
        assertThat(summary.get("topThemes")).anySatisfy(t -> assertThat(t.get("keyword").asText()).isEqualTo("calidad"));

        String generateBody = objectMapper.writeValueAsString(Map.of(
                "platformId", platformId,
                "productOrService", "zapatillas running",
                "objective", "ENGAGEMENT",
                "tone", "CASUAL",
                "variantCount", 2
        ));

        mockMvc.perform(post("/api/comments/generate-ideas").header("Authorization", "Bearer " + token)
                        .contentType("application/json").content(generateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].generatedTitle").isNotEmpty());
    }

    @Test
    void listFiltersBySentiment() throws Exception {
        String token = registerAndLogin("comments.filter@example.com");
        Long platformId = instagramPlatformId(token);

        mockMvc.perform(post("/api/comments").header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("platformId", platformId, "text", "Pésimo, terrible experiencia"))))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/comments").header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("platformId", platformId, "text", "Excelente, me encantó"))))
                .andExpect(status().isCreated());

        MvcResult filtered = mockMvc.perform(get("/api/comments")
                        .header("Authorization", "Bearer " + token)
                        .param("platformId", platformId.toString())
                        .param("sentiment", "NEGATIVE")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode content = objectMapper.readTree(filtered.getResponse().getContentAsString()).get("content");
        assertThat(content).isNotEmpty();
        for (JsonNode comment : content) {
            assertThat(comment.get("sentiment").asText()).isEqualTo("NEGATIVE");
        }
    }
}
