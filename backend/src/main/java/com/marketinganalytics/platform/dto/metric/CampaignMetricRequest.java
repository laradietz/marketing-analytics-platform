package com.marketinganalytics.platform.dto.metric;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CampaignMetricRequest(
        @NotNull(message = "La fecha es obligatoria")
        LocalDate recordedDate,

        @NotNull @Min(value = 0, message = "Las impresiones no pueden ser negativas")
        Long impressions,

        @NotNull @Min(value = 0, message = "El alcance no puede ser negativo")
        Long reach,

        @NotNull @Min(value = 0, message = "Los clics no pueden ser negativos")
        Long clicks,

        @NotNull @Min(value = 0, message = "Las conversiones no pueden ser negativas")
        Long conversions,

        @NotNull @DecimalMin(value = "0.0", message = "El gasto no puede ser negativo")
        BigDecimal spend,

        @NotNull @DecimalMin(value = "0.0", message = "Los ingresos no pueden ser negativos")
        BigDecimal revenue,

        @Min(0) Long likes,
        @Min(0) Long comments,
        @Min(0) Long shares,
        @Min(0) Long saves
) {
}
