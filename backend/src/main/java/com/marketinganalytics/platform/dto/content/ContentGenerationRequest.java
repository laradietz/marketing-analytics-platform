package com.marketinganalytics.platform.dto.content;

import com.marketinganalytics.platform.entity.enums.CampaignObjective;
import com.marketinganalytics.platform.entity.enums.ContentTone;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ContentGenerationRequest(
        @NotBlank(message = "El producto o servicio es obligatorio")
        String productOrService,

        @NotBlank(message = "El público objetivo es obligatorio")
        String targetAudience,

        @NotNull(message = "La plataforma es obligatoria")
        Long platformId,

        @NotNull(message = "El objetivo es obligatorio")
        CampaignObjective objective,

        @NotNull(message = "El tono es obligatorio")
        ContentTone tone,

        String callToAction,

        Integer variantCount
) {
}
