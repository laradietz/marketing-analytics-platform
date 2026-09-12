package com.marketinganalytics.platform.dto.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record ReportGenerateRequest(
        @NotBlank(message = "El nombre del reporte es obligatorio")
        String name,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate periodStart,

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDate periodEnd,

        List<Long> campaignIds,

        List<Long> platformIds
) {
}
