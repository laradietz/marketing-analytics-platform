package com.marketinganalytics.platform.mapper;

import com.marketinganalytics.platform.dto.recommendation.RecommendationResponse;
import com.marketinganalytics.platform.entity.Recommendation;

public final class RecommendationMapper {

    private RecommendationMapper() {
    }

    public static RecommendationResponse toResponse(Recommendation r) {
        return new RecommendationResponse(
                r.getId(),
                r.getTitle(),
                r.getDescription(),
                r.getType(),
                r.getPriority(),
                r.getRelatedMetricName(),
                r.getRelatedMetricValue(),
                r.getCampaign() != null ? r.getCampaign().getId() : null,
                r.getCampaign() != null ? r.getCampaign().getName() : null,
                r.getPlatform() != null ? r.getPlatform().getId() : null,
                r.getPlatform() != null ? r.getPlatform().getName() : null,
                r.getStatus(),
                r.getCreatedAt()
        );
    }
}
