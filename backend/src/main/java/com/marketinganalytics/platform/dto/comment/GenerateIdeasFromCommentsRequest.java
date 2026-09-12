package com.marketinganalytics.platform.dto.comment;

import com.marketinganalytics.platform.entity.enums.CampaignObjective;
import com.marketinganalytics.platform.entity.enums.ContentTone;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GenerateIdeasFromCommentsRequest(
        @NotNull(message = "La plataforma es obligatoria")
        Long platformId,

        @NotBlank(message = "El producto o servicio es obligatorio")
        String productOrService,

        @NotNull(message = "El objetivo es obligatorio")
        CampaignObjective objective,

        @NotNull(message = "El tono es obligatorio")
        ContentTone tone,

        Integer variantCount
) {
}
