package com.marketinganalytics.platform.dto.content;

import com.marketinganalytics.platform.dto.platform.PlatformResponse;
import com.marketinganalytics.platform.entity.enums.CampaignObjective;
import com.marketinganalytics.platform.entity.enums.ContentStatus;

import java.time.Instant;
import java.time.LocalDate;

public record ContentResponse(
        Long id,
        String title,
        String copyText,
        PlatformResponse platform,
        CampaignObjective objective,
        ContentStatus status,
        LocalDate scheduledDate,
        LocalDate publishedDate,
        String hashtags,
        String ctaText,
        Long campaignId,
        String campaignName,
        String createdByName,
        Instant createdAt,
        Instant updatedAt
) {
}
