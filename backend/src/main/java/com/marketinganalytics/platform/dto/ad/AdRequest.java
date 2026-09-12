package com.marketinganalytics.platform.dto.ad;

import com.marketinganalytics.platform.entity.enums.AdFormat;
import com.marketinganalytics.platform.entity.enums.AdStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String name,

        @NotNull(message = "El formato es obligatorio")
        AdFormat format,

        String headline,
        String body,
        String ctaLabel,
        AdStatus status
) {
}
