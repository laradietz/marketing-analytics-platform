package com.marketinganalytics.platform.dto.campaign;

import com.marketinganalytics.platform.dto.metric.DerivedMetrics;
import com.marketinganalytics.platform.dto.platform.PlatformResponse;
import com.marketinganalytics.platform.entity.enums.CampaignObjective;
import com.marketinganalytics.platform.entity.enums.CampaignStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record CampaignResponse(
        Long id,
        String name,
        String description,
        PlatformResponse platform,
        CampaignObjective objective,
        BigDecimal budget,
        LocalDate startDate,
        LocalDate endDate,
        CampaignStatus status,
        String targetAudience,
        Long ownerId,
        String ownerName,
        String notes,
        String externalCampaignId,
        DerivedMetrics metrics,
        Instant createdAt,
        Instant updatedAt
) {
}
