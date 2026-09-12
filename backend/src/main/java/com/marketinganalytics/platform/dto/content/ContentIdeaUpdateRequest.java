package com.marketinganalytics.platform.dto.content;

import com.marketinganalytics.platform.entity.enums.ContentIdeaStatus;
import jakarta.validation.constraints.NotBlank;

public record ContentIdeaUpdateRequest(
        @NotBlank(message = "El título es obligatorio")
        String generatedTitle,

        @NotBlank(message = "El copy es obligatorio")
        String generatedCopy,

        String generatedCta,

        String generatedHashtags,

        ContentIdeaStatus status
) {
}
