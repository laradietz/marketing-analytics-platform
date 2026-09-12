package com.marketinganalytics.platform.dto.report;

import com.marketinganalytics.platform.dto.metric.DerivedMetrics;
import com.marketinganalytics.platform.dto.recommendation.RecommendationResponse;

import java.time.LocalDate;
import java.util.List;

/** Serialized as {@code Report.summaryJson} so a report stays reproducible even if metrics later change. */
public record ReportSummary(
        LocalDate periodStart,
        LocalDate periodEnd,
        DerivedMetrics kpis,
        List<CampaignBreakdown> campaigns,
        List<RecommendationResponse> recommendations
) {
    public record CampaignBreakdown(Long campaignId, String campaignName, String platformName, String status,
                                     DerivedMetrics metrics) {
    }
}
