package com.marketinganalytics.platform.service.integration;

/** Result of exchanging an OAuth authorization code (or refreshing) for tokens. */
public record OAuthTokenResult(String accessToken, String refreshToken, Long expiresInSeconds, String externalAccountId) {
}
