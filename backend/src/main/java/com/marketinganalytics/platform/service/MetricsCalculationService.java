package com.marketinganalytics.platform.service;

import com.marketinganalytics.platform.dto.metric.DerivedMetrics;
import com.marketinganalytics.platform.dto.metric.MetricTotals;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pure calculation of derived marketing metrics from raw totals.
 * <p>
 * Every ratio guards against division by zero by returning {@link BigDecimal#ZERO}
 * instead of throwing or producing NaN/Infinity, since these values feed
 * directly into JSON responses and charts.
 */
public final class MetricsCalculationService {

    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private MetricsCalculationService() {
    }

    /** clicks / impressions * 100 */
    public static BigDecimal ctr(long clicks, long impressions) {
        if (impressions <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(clicks)
                .divide(BigDecimal.valueOf(impressions), SCALE, ROUNDING)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, ROUNDING);
    }

    /** spend / clicks */
    public static BigDecimal cpc(BigDecimal spend, long clicks) {
        if (clicks <= 0 || spend == null) {
            return BigDecimal.ZERO;
        }
        return spend.divide(BigDecimal.valueOf(clicks), SCALE, ROUNDING).setScale(2, ROUNDING);
    }

    /** spend / impressions * 1000 */
    public static BigDecimal cpm(BigDecimal spend, long impressions) {
        if (impressions <= 0 || spend == null) {
            return BigDecimal.ZERO;
        }
        return spend.divide(BigDecimal.valueOf(impressions), SCALE, ROUNDING)
                .multiply(BigDecimal.valueOf(1000))
                .setScale(2, ROUNDING);
    }

    /** conversions / clicks * 100 */
    public static BigDecimal conversionRate(long conversions, long clicks) {
        if (clicks <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(conversions)
                .divide(BigDecimal.valueOf(clicks), SCALE, ROUNDING)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, ROUNDING);
    }

    /** spend / conversions */
    public static BigDecimal cpa(BigDecimal spend, long conversions) {
        if (conversions <= 0 || spend == null) {
            return BigDecimal.ZERO;
        }
        return spend.divide(BigDecimal.valueOf(conversions), SCALE, ROUNDING).setScale(2, ROUNDING);
    }

    /** revenue / spend */
    public static BigDecimal roas(BigDecimal revenue, BigDecimal spend) {
        if (spend == null || spend.compareTo(BigDecimal.ZERO) <= 0 || revenue == null) {
            return BigDecimal.ZERO;
        }
        return revenue.divide(spend, SCALE, ROUNDING).setScale(2, ROUNDING);
    }

    public static DerivedMetrics derive(MetricTotals totals) {
        return new DerivedMetrics(
                totals,
                ctr(totals.clicks(), totals.impressions()),
                cpc(totals.spend(), totals.clicks()),
                cpm(totals.spend(), totals.impressions()),
                conversionRate(totals.conversions(), totals.clicks()),
                cpa(totals.spend(), totals.conversions()),
                roas(totals.revenue(), totals.spend())
        );
    }
}
