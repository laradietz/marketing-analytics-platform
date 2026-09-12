package com.marketinganalytics.platform.dto.content;

import com.marketinganalytics.platform.entity.enums.CampaignObjective;
import com.marketinganalytics.platform.entity.enums.ContentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ContentRequest(
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 200)
        String title,

        String copyText,

        @NotNull(message = "La plataforma es obligatoria")
        Long platformId,

        CampaignObjective objective,

        @NotNull(message = "El estado es obligatorio")
        ContentStatus status,

        LocalDate scheduledDate,

        String hashtags,

        String ctaText,

        Long campaignId
) {
}
