package com.marketinganalytics.platform.dto.dashboard;

import com.marketinganalytics.platform.dto.activity.ActivityLogResponse;
import com.marketinganalytics.platform.dto.lead.LeadResponse;
import com.marketinganalytics.platform.dto.metric.DerivedMetrics;
import com.marketinganalytics.platform.dto.recommendation.RecommendationResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DashboardResponse(
        DerivedMetrics kpis,
        List<DailyBreakdownPoint> dailyBreakdown,
        List<PlatformPerformance> platformPerformance,
        List<CampaignRanking> topCampaigns,
        List<CampaignRanking> bottomCampaigns,
        List<BudgetDistributionItem> budgetDistribution,
        List<LeadResponse> recentLeads,
        List<RecommendationResponse> recentRecommendations,
        List<ActivityLogResponse> recentActivity
) {
    /**
     * One day's raw totals across every campaign matching the dashboard filter.
     * Kept raw (not pre-reduced to a single metric) so the frontend can plot
     * whichever metric the user picks — including ratios like CTR/ROAS,
     * computed client-side from these totals — without another round trip.
     */
    public record DailyBreakdownPoint(LocalDate date, long impressions, long clicks, long conversions,
                                       BigDecimal spend, BigDecimal revenue) {
    }

    public record PlatformPerformance(Long platformId, String platformName, String colorHex, BigDecimal spend,
                                       BigDecimal revenue, BigDecimal roas, BigDecimal ctr, long conversions) {
    }

    public record CampaignRanking(Long campaignId, String campaignName, String platformName, BigDecimal roas,
                                   BigDecimal spend, BigDecimal revenue) {
    }

    public record BudgetDistributionItem(Long platformId, String platformName, String colorHex, BigDecimal spend,
                                          BigDecimal percentage) {
    }
}
