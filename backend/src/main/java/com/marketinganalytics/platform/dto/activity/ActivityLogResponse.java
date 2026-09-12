package com.marketinganalytics.platform.dto.activity;

import java.time.Instant;

public record ActivityLogResponse(
        Long id,
        String userName,
        String action,
        String entityType,
        Long entityId,
        String description,
        Instant createdAt
) {
}
