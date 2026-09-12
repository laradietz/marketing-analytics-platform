package com.marketinganalytics.platform.service.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Verifies the Graph API insights response is parsed correctly — the part of
 * this adapter that can be tested without a real Meta app, since it exercises
 * our own JSON-to-{@link ExternalInsight} mapping rather than Meta's servers.
 */
class MetaAdsSyncProviderTest {

    @Test
    void parsesImpressionsClicksSpendAndPurchaseActionsFromAGraphApiResponse() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        String responseBody = """
                {
                  "data": [
                    {
                      "date_start": "2026-01-01",
                      "date_stop": "2026-01-01",
                      "impressions": "10000",
                      "clicks": "250",
                      "spend": "150.50",
                      "actions": [
                        {"action_type": "link_click", "value": "180"},
                        {"action_type": "purchase", "value": "12"}
                      ],
                      "action_values": [
                        {"action_type": "purchase", "value": "480.00"}
                      ]
                    }
                  ]
                }
                """;

        server.expect(method(HttpMethod.GET))
                .andExpect(queryParam("fields", "impressions,clicks,spend,actions,action_values"))
                .andExpect(queryParam("time_range", "%7B%22since%22:%222026-01-01%22,%22until%22:%222026-01-01%22%7D"))
                .andExpect(queryParam("time_increment", "1"))
                .andExpect(queryParam("access_token", "fake-token"))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        MetaAdsSyncProvider provider = new MetaAdsSyncProvider(builder);
        List<ExternalInsight> insights = provider.fetchInsights(
                "fake-token", null, "123456", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 1));

        server.verify();
        assertThat(insights).hasSize(1);
        ExternalInsight insight = insights.get(0);
        assertThat(insight.date()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(insight.impressions()).isEqualTo(10000);
        assertThat(insight.clicks()).isEqualTo(250);
        assertThat(insight.spend()).isEqualByComparingTo("150.50");
        assertThat(insight.conversions()).isEqualTo(12);
        assertThat(insight.revenue()).isEqualByComparingTo("480.00");
    }

    @Test
    void reportsNotConfiguredWhenCredentialsAreMissing() {
        MetaAdsSyncProvider provider = new MetaAdsSyncProvider(RestClient.builder());
        assertThat(provider.isConfigured()).isFalse();
    }
}
