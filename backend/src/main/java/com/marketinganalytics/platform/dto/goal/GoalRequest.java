package com.marketinganalytics.platform.dto.goal;

import com.marketinganalytics.platform.entity.enums.GoalMetric;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GoalRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String name,

        @NotNull(message = "El tipo de métrica es obligatorio")
        GoalMetric metricType,

        @NotNull(message = "El valor objetivo es obligatorio")
        @DecimalMin(value = "0.0", message = "El valor objetivo no puede ser negativo")
        BigDecimal targetValue,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate periodStart,

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDate periodEnd,

        Long campaignId
) {
}
