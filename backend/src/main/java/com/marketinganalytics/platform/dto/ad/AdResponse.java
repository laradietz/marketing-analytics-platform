package com.marketinganalytics.platform.dto.ad;

import com.marketinganalytics.platform.entity.enums.AdFormat;
import com.marketinganalytics.platform.entity.enums.AdStatus;

import java.time.Instant;

public record AdResponse(
        Long id,
        Long campaignId,
        String name,
        AdFormat format,
        String headline,
        String body,
        String ctaLabel,
        AdStatus status,
        Instant createdAt
) {
}
