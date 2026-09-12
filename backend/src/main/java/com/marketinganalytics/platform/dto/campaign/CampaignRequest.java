package com.marketinganalytics.platform.dto.campaign;

import com.marketinganalytics.platform.entity.enums.CampaignObjective;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CampaignRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
        String name,

        String description,

        @NotNull(message = "La plataforma es obligatoria")
        Long platformId,

        @NotNull(message = "El objetivo es obligatorio")
        CampaignObjective objective,

        @NotNull(message = "El presupuesto es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El presupuesto no puede ser negativo")
        BigDecimal budget,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate startDate,

        LocalDate endDate,

        String targetAudience,

        Long ownerId,

        String notes,

        String externalCampaignId
) {
}
