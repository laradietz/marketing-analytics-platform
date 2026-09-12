package com.marketinganalytics.platform.service.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketinganalytics.platform.entity.enums.IntegrationProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Real adapter for the Google Ads API (REST transport). Requires a
 * manager-account developer token approved for at least "Basic access" — an
 * approval step only Google can grant, done from the Google Ads UI, separate
 * from creating OAuth credentials.
 * <p>
 * Required env vars: {@code GOOGLE_ADS_CLIENT_ID}, {@code GOOGLE_ADS_CLIENT_SECRET},
 * {@code GOOGLE_ADS_DEVELOPER_TOKEN}, {@code GOOGLE_ADS_REDIRECT_URI}, and
 * optionally {@code GOOGLE_ADS_LOGIN_CUSTOMER_ID} (manager account id, if the
 * connected account is managed under an MCC). The connection's external
 * account id is the 10-digit Google Ads customer id (no dashes).
 * Docs: https://developers.google.com/google-ads/api/rest/overview
 */
@Service
public class GoogleAdsSyncProvider implements PlatformSyncProvider {

    private static final String API_VERSION = "v17";

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GoogleAdsSyncProvider(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Value("${app.integrations.google-ads.client-id:}")
    private String clientId;

    @Value("${app.integrations.google-ads.client-secret:}")
    private String clientSecret;

    @Value("${app.integrations.google-ads.developer-token:}")
    private String developerToken;

    @Value("${app.integrations.google-ads.login-customer-id:}")
    private String loginCustomerId;

    @Value("${app.integrations.google-ads.redirect-uri}")
    private String redirectUri;

    @Override
    public IntegrationProvider getProvider() {
        return IntegrationProvider.GOOGLE_ADS;
    }

    @Override
    public boolean isConfigured() {
        return StringUtils.hasText(clientId) && StringUtils.hasText(clientSecret) && StringUtils.hasText(developerToken);
    }

    @Override
    public String getRedirectUri() {
        return redirectUri;
    }

    @Override
    public String buildAuthorizationUrl(String state, String redirectUri) {
        requireConfigured();
        return UriComponentsBuilder.fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .queryParam("scope", "https://www.googleapis.com/auth/adwords")
                .queryParam("state", state)
                .build()
                .toUriString();
    }

    @Override
    public OAuthTokenResult exchangeCodeForToken(String code, String redirectUri) {
        requireConfigured();
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("code", code);
        form.add("redirect_uri", redirectUri);
        form.add("grant_type", "authorization_code");

        String response = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(String.class);

        JsonNode json = readJson(response);
        return new OAuthTokenResult(
                json.path("access_token").asText(null),
                json.path("refresh_token").asText(null),
                json.path("expires_in").asLong(3600),
                null
        );
    }

    @Override
    public List<ExternalInsight> fetchInsights(String accessToken, String externalAccountId, String externalCampaignId,
                                                LocalDate from, LocalDate to) {
        String customerId = externalAccountId.replace("-", "");
        String query = """
                SELECT segments.date, metrics.impressions, metrics.clicks, metrics.conversions,
                       metrics.cost_micros, metrics.conversions_value
                FROM campaign
                WHERE campaign.id = %s AND segments.date BETWEEN '%s' AND '%s'
                """.formatted(externalCampaignId, from, to);

        var requestSpec = restClient.post()
                .uri("https://googleads.googleapis.com/" + API_VERSION + "/customers/" + customerId + "/googleAds:searchStream")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .header("developer-token", developerToken);
        if (StringUtils.hasText(loginCustomerId)) {
            requestSpec = requestSpec.header("login-customer-id", loginCustomerId.replace("-", ""));
        }

        String response = requestSpec
                .body(Map.of("query", query))
                .retrieve()
                .body(String.class);

        JsonNode json = readJson(response);
        List<ExternalInsight> insights = new ArrayList<>();
        // searchStream returns an array of batches, each with a "results" array.
        for (JsonNode batch : json) {
            for (JsonNode row : batch.path("results")) {
                JsonNode metrics = row.path("metrics");
                BigDecimal spend = BigDecimal.valueOf(metrics.path("costMicros").asLong(0))
                        .divide(BigDecimal.valueOf(1_000_000), 2, RoundingMode.HALF_UP);
                insights.add(new ExternalInsight(
                        LocalDate.parse(row.path("segments").path("date").asText()),
                        metrics.path("impressions").asLong(0),
                        metrics.path("clicks").asLong(0),
                        Math.round(metrics.path("conversions").asDouble(0)),
                        spend,
                        BigDecimal.valueOf(metrics.path("conversionsValue").asDouble(0))
                ));
            }
        }
        return insights;
    }

    private JsonNode readJson(String body) {
        try {
            return objectMapper.readTree(body);
        } catch (Exception ex) {
            throw new IllegalStateException("Respuesta inesperada de Google Ads: " + body, ex);
        }
    }

    private void requireConfigured() {
        if (!isConfigured()) {
            throw new IllegalStateException(
                    "Google Ads no está configurado. Definí GOOGLE_ADS_CLIENT_ID, GOOGLE_ADS_CLIENT_SECRET y GOOGLE_ADS_DEVELOPER_TOKEN.");
        }
    }
}
