package com.marketinganalytics.platform.dto.integration;

import com.marketinganalytics.platform.entity.enums.ConnectionStatus;
import com.marketinganalytics.platform.entity.enums.IntegrationProvider;

import java.time.Instant;

public record PlatformConnectionResponse(
        Long platformId,
        String platformName,
        String colorHex,
        IntegrationProvider provider,
        ConnectionStatus status,
        String externalAccountId,
        Instant lastSyncedAt,
        String lastSyncMessage,
        boolean providerConfigured
) {
}
