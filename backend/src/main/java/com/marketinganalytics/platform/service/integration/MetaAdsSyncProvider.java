package com.marketinganalytics.platform.service.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketinganalytics.platform.entity.enums.IntegrationProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Real adapter for the Meta Marketing API (Graph API). Needs a Meta app with
 * the "Marketing API" product added and {@code ads_read} permission granted
 * on the ad account being connected.
 * <p>
 * Required env vars: {@code META_CLIENT_ID}, {@code META_CLIENT_SECRET},
 * {@code META_REDIRECT_URI}. Docs: https://developers.facebook.com/docs/marketing-apis
 */
@Service
public class MetaAdsSyncProvider implements PlatformSyncProvider {

    private static final String API_VERSION = "v21.0";
    private static final String GRAPH_BASE = "https://graph.facebook.com/" + API_VERSION;
    private static final Set<String> CONVERSION_ACTION_TYPES = Set.of(
            "purchase", "offsite_conversion.fb_pixel_purchase", "lead", "offsite_conversion.fb_pixel_lead");

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MetaAdsSyncProvider(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Value("${app.integrations.meta.client-id:}")
    private String clientId;

    @Value("${app.integrations.meta.client-secret:}")
    private String clientSecret;

    @Value("${app.integrations.meta.redirect-uri}")
    private String redirectUri;

    @Override
    public IntegrationProvider getProvider() {
        return IntegrationProvider.META_ADS;
    }

    @Override
    public boolean isConfigured() {
        return StringUtils.hasText(clientId) && StringUtils.hasText(clientSecret);
    }

    @Override
    public String getRedirectUri() {
        return redirectUri;
    }

    @Override
    public String buildAuthorizationUrl(String state, String redirectUri) {
        requireConfigured();
        return UriComponentsBuilder.fromUriString("https://www.facebook.com/" + API_VERSION + "/dialog/oauth")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .queryParam("scope", "ads_read")
                .queryParam("response_type", "code")
                .build()
                .toUriString();
    }

    @Override
    public OAuthTokenResult exchangeCodeForToken(String code, String redirectUri) {
        requireConfigured();
        String response = restClient.get()
                .uri(UriComponentsBuilder.fromUriString(GRAPH_BASE + "/oauth/access_token")
                        .queryParam("client_id", clientId)
                        .queryParam("client_secret", clientSecret)
                        .queryParam("redirect_uri", redirectUri)
                        .queryParam("code", code)
                        .build()
                        .toUri())
                .retrieve()
                .body(String.class);

        JsonNode json = readJson(response);
        String accessToken = json.path("access_token").asText(null);
        long expiresIn = json.path("expires_in").asLong(5184000); // Meta long-lived tokens default ~60 days
        return new OAuthTokenResult(accessToken, null, expiresIn, null);
    }

    @Override
    public List<ExternalInsight> fetchInsights(String accessToken, String externalAccountId, String externalCampaignId,
                                                LocalDate from, LocalDate to) {
        String timeRange = "{\"since\":\"" + from + "\",\"until\":\"" + to + "\"}";
        // Built with build().encode() (never buildAndExpand()): time_range's literal
        // braces would otherwise be misread as a URI template variable to expand.
        String response = restClient.get()
                .uri(UriComponentsBuilder.fromUriString(GRAPH_BASE + "/" + externalCampaignId + "/insights")
                        .queryParam("fields", "impressions,clicks,spend,actions,action_values")
                        .queryParam("time_range", timeRange)
                        .queryParam("time_increment", 1)
                        .queryParam("access_token", accessToken)
                        .build()
                        .encode()
                        .toUri())
                .retrieve()
                .body(String.class);

        JsonNode json = readJson(response);
        List<ExternalInsight> insights = new ArrayList<>();
        for (JsonNode row : json.path("data")) {
            insights.add(new ExternalInsight(
                    LocalDate.parse(row.path("date_start").asText()),
                    row.path("impressions").asLong(0),
                    row.path("clicks").asLong(0),
                    sumActions(row.path("actions"), CONVERSION_ACTION_TYPES),
                    new BigDecimal(row.path("spend").asText("0")),
                    sumActionValues(row.path("action_values"), CONVERSION_ACTION_TYPES)
            ));
        }
        return insights;
    }

    private long sumActions(JsonNode actions, Set<String> types) {
        long total = 0;
        for (JsonNode action : actions) {
            if (types.contains(action.path("action_type").asText())) {
                total += action.path("value").asLong(0);
            }
        }
        return total;
    }

    private BigDecimal sumActionValues(JsonNode actionValues, Set<String> types) {
        BigDecimal total = BigDecimal.ZERO;
        for (JsonNode action : actionValues) {
            if (types.contains(action.path("action_type").asText())) {
                total = total.add(new BigDecimal(action.path("value").asText("0")));
            }
        }
        return total;
    }

    private JsonNode readJson(String body) {
        try {
            return objectMapper.readTree(body);
        } catch (Exception ex) {
            throw new IllegalStateException("Respuesta inesperada de Meta Ads: " + body, ex);
        }
    }

    private void requireConfigured() {
        if (!isConfigured()) {
            throw new IllegalStateException(
                    "Meta Ads no está configurado. Definí META_CLIENT_ID y META_CLIENT_SECRET.");
        }
    }
}
