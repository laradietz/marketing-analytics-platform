package com.marketinganalytics.platform.dto.budget;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BudgetRequest(
        @NotNull(message = "La fecha de inicio del período es obligatoria")
        LocalDate periodStart,

        @NotNull(message = "La fecha de fin del período es obligatoria")
        LocalDate periodEnd,

        @NotNull(message = "El monto planificado es obligatorio")
        @DecimalMin(value = "0.0", message = "El monto no puede ser negativo")
        BigDecimal plannedAmount,

        String notes
) {
}
