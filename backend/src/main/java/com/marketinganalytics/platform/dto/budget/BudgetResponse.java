package com.marketinganalytics.platform.dto.budget;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BudgetResponse(
        Long id,
        Long campaignId,
        LocalDate periodStart,
        LocalDate periodEnd,
        BigDecimal plannedAmount,
        BigDecimal actualSpend,
        String notes
) {
}
