package com.marketinganalytics.platform.dto.report;

import java.time.Instant;
import java.time.LocalDate;

public record ReportResponse(
        Long id,
        String name,
        LocalDate periodStart,
        LocalDate periodEnd,
        String generatedByName,
        ReportSummary summary,
        Instant createdAt
) {
}
