package com.marketinganalytics.platform.dto.goal;

import com.marketinganalytics.platform.entity.enums.GoalMetric;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GoalResponse(
        Long id,
        String name,
        GoalMetric metricType,
        BigDecimal targetValue,
        BigDecimal currentValue,
        BigDecimal progressPercentage,
        LocalDate periodStart,
        LocalDate periodEnd,
        Long campaignId,
        String campaignName
) {
}
