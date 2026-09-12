package com.marketinganalytics.platform.dto.recommendation;

import com.marketinganalytics.platform.entity.enums.RecommendationPriority;
import com.marketinganalytics.platform.entity.enums.RecommendationStatus;
import com.marketinganalytics.platform.entity.enums.RecommendationType;

import java.math.BigDecimal;
import java.time.Instant;

public record RecommendationResponse(
        Long id,
        String title,
        String description,
        RecommendationType type,
        RecommendationPriority priority,
        String relatedMetricName,
        BigDecimal relatedMetricValue,
        Long campaignId,
        String campaignName,
        Long platformId,
        String platformName,
        RecommendationStatus status,
        Instant createdAt
) {
}
