package com.marketinganalytics.platform.service;

import com.marketinganalytics.platform.dto.metric.DerivedMetrics;
import com.marketinganalytics.platform.dto.metric.MetricTotals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsCalculationServiceTest {

    @Test
    @DisplayName("CTR = clicks / impressions * 100")
    void computesCtr() {
        assertThat(MetricsCalculationService.ctr(50, 1000)).isEqualByComparingTo("5.00");
    }

    @Test
    @DisplayName("CTR is zero (not NaN/Infinity) when impressions are zero")
    void ctrHandlesZeroImpressions() {
        assertThat(MetricsCalculationService.ctr(50, 0)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("CPC = spend / clicks")
    void computesCpc() {
        assertThat(MetricsCalculationService.cpc(new BigDecimal("500.00"), 100)).isEqualByComparingTo("5.00");
    }

    @Test
    @DisplayName("CPC is zero when there are no clicks")
    void cpcHandlesZeroClicks() {
        assertThat(MetricsCalculationService.cpc(new BigDecimal("500.00"), 0)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("CPM = spend / impressions * 1000")
    void computesCpm() {
        assertThat(MetricsCalculationService.cpm(new BigDecimal("200.00"), 10_000)).isEqualByComparingTo("20.00");
    }

    @Test
    @DisplayName("CPM is zero when there are no impressions")
    void cpmHandlesZeroImpressions() {
        assertThat(MetricsCalculationService.cpm(new BigDecimal("200.00"), 0)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Conversion rate = conversions / clicks * 100")
    void computesConversionRate() {
        assertThat(MetricsCalculationService.conversionRate(20, 200)).isEqualByComparingTo("10.00");
    }

    @Test
    @DisplayName("Conversion rate is zero when there are no clicks")
    void conversionRateHandlesZeroClicks() {
        assertThat(MetricsCalculationService.conversionRate(20, 0)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("CPA = spend / conversions")
    void computesCpa() {
        assertThat(MetricsCalculationService.cpa(new BigDecimal("1000.00"), 20)).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("CPA is zero when there are no conversions")
    void cpaHandlesZeroConversions() {
        assertThat(MetricsCalculationService.cpa(new BigDecimal("1000.00"), 0)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("ROAS = revenue / spend")
    void computesRoas() {
        assertThat(MetricsCalculationService.roas(new BigDecimal("300000"), new BigDecimal("100000")))
                .isEqualByComparingTo("3.00");
    }

    @Test
    @DisplayName("ROAS is zero (not Infinity) when spend is zero")
    void roasHandlesZeroSpend() {
        assertThat(MetricsCalculationService.roas(new BigDecimal("300000"), BigDecimal.ZERO))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("derive() combines every ratio from raw totals")
    void derivesAllMetricsFromTotals() {
        MetricTotals totals = new MetricTotals(10_000, 8_000, 500, 25,
                new BigDecimal("1000.00"), new BigDecimal("3000.00"), 100, 10, 5, 20);

        DerivedMetrics derived = MetricsCalculationService.derive(totals);

        assertThat(derived.ctr()).isEqualByComparingTo("5.00");
        assertThat(derived.cpc()).isEqualByComparingTo("2.00");
        assertThat(derived.cpm()).isEqualByComparingTo("100.00");
        assertThat(derived.conversionRate()).isEqualByComparingTo("5.00");
        assertThat(derived.cpa()).isEqualByComparingTo("40.00");
        assertThat(derived.roas()).isEqualByComparingTo("3.00");
    }

    @Test
    @DisplayName("derive() never produces NaN/Infinity when everything is zero")
    void derivesSafelyFromAllZeroTotals() {
        DerivedMetrics derived = MetricsCalculationService.derive(MetricTotals.ZERO);

        assertThat(derived.ctr()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(derived.cpc()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(derived.cpm()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(derived.conversionRate()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(derived.cpa()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(derived.roas()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
