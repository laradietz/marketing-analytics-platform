package com.marketinganalytics.platform.dto.metric;

import java.math.BigDecimal;

/**
 * CTR/CPC/CPM/conversion-rate/CPA/ROAS derived from a {@link MetricTotals}.
 * Every field is guaranteed finite (never NaN/Infinity) — see
 * {@link com.marketinganalytics.platform.service.MetricsCalculationService}.
 */
public record DerivedMetrics(
        MetricTotals totals,
        BigDecimal ctr,
        BigDecimal cpc,
        BigDecimal cpm,
        BigDecimal conversionRate,
        BigDecimal cpa,
        BigDecimal roas
) {
}
