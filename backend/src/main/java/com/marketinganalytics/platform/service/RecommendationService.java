package com.marketinganalytics.platform.service;

import com.marketinganalytics.platform.dto.common.PageResponse;
import com.marketinganalytics.platform.dto.recommendation.RecommendationResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RecommendationService {
    /** Re-analyzes current campaign/platform metrics and (re)generates recommendations. */
    List<RecommendationResponse> generate();
    PageResponse<RecommendationResponse> list(Pageable pageable);
    List<RecommendationResponse> getRecent();
    RecommendationResponse updateStatus(Long id, com.marketinganalytics.platform.entity.enums.RecommendationStatus status);
}
