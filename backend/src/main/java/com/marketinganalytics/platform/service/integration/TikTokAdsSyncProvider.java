package com.marketinganalytics.platform.service.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketinganalytics.platform.entity.enums.IntegrationProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Real adapter for the TikTok Business (Marketing) API. Requires a TikTok
 * for Business "developer app" with the Reporting scope approved.
 * <p>
 * Required env vars: {@code TIKTOK_APP_ID}, {@code TIKTOK_APP_SECRET},
 * {@code TIKTOK_REDIRECT_URI}. The connection's external account id is the
 * TikTok {@code advertiser_id}.
 * <p>
 * Note: TikTok's basic integrated report does not include revenue unless the
 * advertiser has configured value-based Events API tracking — until that's
 * set up on the connected account, revenue for this provider is reported as
 * zero rather than guessed. Docs: https://business-api.tiktok.com/portal/docs
 */
@Service
public class TikTokAdsSyncProvider implements PlatformSyncProvider {

    private static final String API_BASE = "https://business-api.tiktok.com/open_api/v1.3";

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TikTokAdsSyncProvider(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Value("${app.integrations.tiktok.app-id:}")
    private String appId;

    @Value("${app.integrations.tiktok.app-secret:}")
    private String appSecret;

    @Value("${app.integrations.tiktok.redirect-uri}")
    private String redirectUri;

    @Override
    public IntegrationProvider getProvider() {
        return IntegrationProvider.TIKTOK_ADS;
    }

    @Override
    public boolean isConfigured() {
        return StringUtils.hasText(appId) && StringUtils.hasText(appSecret);
    }

    @Override
    public String getRedirectUri() {
        return redirectUri;
    }

    @Override
    public String buildAuthorizationUrl(String state, String redirectUri) {
        requireConfigured();
        return UriComponentsBuilder.fromUriString("https://business-api.tiktok.com/portal/auth")
                .queryParam("app_id", appId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .build()
                .toUriString();
    }

    @Override
    public OAuthTokenResult exchangeCodeForToken(String code, String redirectUri) {
        requireConfigured();
        String response = restClient.post()
                .uri(API_BASE + "/oauth2/access_token/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("app_id", appId, "secret", appSecret, "auth_code", code))
                .retrieve()
                .body(String.class);

        JsonNode data = readJson(response).path("data");
        String advertiserId = data.path("advertiser_ids").isArray() && !data.path("advertiser_ids").isEmpty()
                ? data.path("advertiser_ids").get(0).asText()
                : null;
        return new OAuthTokenResult(data.path("access_token").asText(null), null, null, advertiserId);
    }

    @Override
    public List<ExternalInsight> fetchInsights(String accessToken, String externalAccountId, String externalCampaignId,
                                                LocalDate from, LocalDate to) {
        String filters = "[{\"field_name\":\"campaign_ids\",\"filter_type\":\"IN\",\"filter_value\":\"[\\\"" + externalCampaignId + "\\\"]\"}]";

        String response = restClient.get()
                .uri(UriComponentsBuilder.fromUriString(API_BASE + "/report/integrated/get/")
                        .queryParam("advertiser_id", externalAccountId)
                        .queryParam("report_type", "BASIC")
                        .queryParam("data_level", "AUCTION_CAMPAIGN")
                        .queryParam("dimensions", "[\"campaign_id\",\"stat_time_day\"]")
                        .queryParam("metrics", "[\"impressions\",\"clicks\",\"spend\",\"conversion\"]")
                        .queryParam("start_date", from.toString())
                        .queryParam("end_date", to.toString())
                        .queryParam("filters", filters)
                        .encode()
                        .build()
                        .toUri())
                .header("Access-Token", accessToken)
                .retrieve()
                .body(String.class);

        JsonNode json = readJson(response);
        List<ExternalInsight> insights = new ArrayList<>();
        for (JsonNode row : json.path("data").path("list")) {
            JsonNode dimensions = row.path("dimensions");
            JsonNode metrics = row.path("metrics");
            String statDay = dimensions.path("stat_time_day").asText().split(" ")[0];
            insights.add(new ExternalInsight(
                    LocalDate.parse(statDay),
                    metrics.path("impressions").asLong(0),
                    metrics.path("clicks").asLong(0),
                    metrics.path("conversion").asLong(0),
                    new BigDecimal(metrics.path("spend").asText("0")),
                    BigDecimal.ZERO
            ));
        }
        return insights;
    }

    private JsonNode readJson(String body) {
        try {
            return objectMapper.readTree(body);
        } catch (Exception ex) {
            throw new IllegalStateException("Respuesta inesperada de TikTok Ads: " + body, ex);
        }
    }

    private void requireConfigured() {
        if (!isConfigured()) {
            throw new IllegalStateException("TikTok Ads no está configurado. Definí TIKTOK_APP_ID y TIKTOK_APP_SECRET.");
        }
    }
}
