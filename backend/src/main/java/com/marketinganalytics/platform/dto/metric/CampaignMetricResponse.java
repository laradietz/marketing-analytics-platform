package com.marketinganalytics.platform.dto.metric;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CampaignMetricResponse(
        Long id,
        LocalDate recordedDate,
        long impressions,
        long reach,
        long clicks,
        long conversions,
        BigDecimal spend,
        BigDecimal revenue,
        long likes,
        long comments,
        long shares,
        long saves,
        BigDecimal ctr,
        BigDecimal cpc,
        BigDecimal cpm,
        BigDecimal conversionRate,
        BigDecimal cpa,
        BigDecimal roas
) {
}
