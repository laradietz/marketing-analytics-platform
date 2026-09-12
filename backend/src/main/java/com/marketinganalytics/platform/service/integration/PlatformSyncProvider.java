package com.marketinganalytics.platform.service.integration;

import com.marketinganalytics.platform.entity.enums.IntegrationProvider;

import java.time.LocalDate;
import java.util.List;

/**
 * Adapter to one real ad platform's OAuth + reporting API. Each
 * implementation is a real, working HTTP client against that platform's
 * public API — it just needs real credentials (client id/secret, developer
 * tokens, etc., supplied via environment variables) to actually authenticate.
 * <p>
 * To add a new platform: implement this interface, register it as a
 * {@code @Service}, and add its {@link IntegrationProvider} + required env
 * vars to the README. Nothing else in {@code PlatformConnectionService} or
 * the controllers needs to change.
 */
public interface PlatformSyncProvider {

    IntegrationProvider getProvider();

    /** Whether real credentials are configured for this provider (vs. only the architecture being present). */
    boolean isConfigured();

    /** The redirect URI registered with this provider's OAuth app (configured via env var). */
    String getRedirectUri();

    String buildAuthorizationUrl(String state, String redirectUri);

    OAuthTokenResult exchangeCodeForToken(String code, String redirectUri);

    /**
     * Fetches per-day performance for one external campaign. {@code accessToken}
     * must already be decrypted and valid (non-expired) — refreshing is the
     * caller's ({@code PlatformConnectionService}) responsibility, this
     * adapter only ever sees plain tokens in memory, never persists them.
     */
    List<ExternalInsight> fetchInsights(String accessToken, String externalAccountId, String externalCampaignId,
                                         LocalDate from, LocalDate to);
}
