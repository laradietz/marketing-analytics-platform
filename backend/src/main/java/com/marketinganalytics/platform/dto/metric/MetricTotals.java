package com.marketinganalytics.platform.dto.metric;

import java.math.BigDecimal;

/**
 * Raw, summable counters for a period/scope. Everything derived (CTR, CPC,
 * ROAS, ...) is computed from these via {@link com.marketinganalytics.platform.service.MetricsCalculationService}
 * rather than stored, so it can never drift out of sync.
 */
public record MetricTotals(
        long impressions,
        long reach,
        long clicks,
        long conversions,
        BigDecimal spend,
        BigDecimal revenue,
        long likes,
        long comments,
        long shares,
        long saves
) {
    public static final MetricTotals ZERO =
            new MetricTotals(0, 0, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO, 0, 0, 0, 0);

    public MetricTotals add(MetricTotals other) {
        return new MetricTotals(
                impressions + other.impressions,
                reach + other.reach,
                clicks + other.clicks,
                conversions + other.conversions,
                spend.add(other.spend),
                revenue.add(other.revenue),
                likes + other.likes,
                comments + other.comments,
                shares + other.shares,
                saves + other.saves
        );
    }
}
