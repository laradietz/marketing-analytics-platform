package com.marketinganalytics.platform.mapper;

import com.marketinganalytics.platform.dto.metric.CampaignMetricResponse;
import com.marketinganalytics.platform.dto.metric.MetricTotals;
import com.marketinganalytics.platform.entity.CampaignMetric;
import com.marketinganalytics.platform.service.MetricsCalculationService;

import java.math.BigDecimal;

public final class CampaignMetricMapper {

    private CampaignMetricMapper() {
    }

    public static CampaignMetricResponse toResponse(CampaignMetric m) {
        BigDecimal spend = m.getSpend() != null ? m.getSpend() : BigDecimal.ZERO;
        BigDecimal revenue = m.getRevenue() != null ? m.getRevenue() : BigDecimal.ZERO;

        return new CampaignMetricResponse(
                m.getId(),
                m.getRecordedDate(),
                m.getImpressions(),
                m.getReach(),
                m.getClicks(),
                m.getConversions(),
                spend,
                revenue,
                m.getLikes(),
                m.getComments(),
                m.getShares(),
                m.getSaves(),
                MetricsCalculationService.ctr(m.getClicks(), m.getImpressions()),
                MetricsCalculationService.cpc(spend, m.getClicks()),
                MetricsCalculationService.cpm(spend, m.getImpressions()),
                MetricsCalculationService.conversionRate(m.getConversions(), m.getClicks()),
                MetricsCalculationService.cpa(spend, m.getConversions()),
                MetricsCalculationService.roas(revenue, spend)
        );
    }

    public static MetricTotals toTotals(CampaignMetric m) {
        return new MetricTotals(
                m.getImpressions(), m.getReach(), m.getClicks(), m.getConversions(),
                m.getSpend() != null ? m.getSpend() : BigDecimal.ZERO,
                m.getRevenue() != null ? m.getRevenue() : BigDecimal.ZERO,
                m.getLikes(), m.getComments(), m.getShares(), m.getSaves()
        );
    }
}
