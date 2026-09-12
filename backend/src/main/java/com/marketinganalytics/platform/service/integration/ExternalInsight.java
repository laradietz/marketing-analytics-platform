package com.marketinganalytics.platform.service.integration;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One day of performance data as returned by an ad platform's reporting API, before it's mapped to a CampaignMetric. */
public record ExternalInsight(
        LocalDate date,
        long impressions,
        long clicks,
        long conversions,
        BigDecimal spend,
        BigDecimal revenue
) {
}
