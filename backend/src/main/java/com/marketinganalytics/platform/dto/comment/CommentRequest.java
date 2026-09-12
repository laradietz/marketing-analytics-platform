package com.marketinganalytics.platform.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CommentRequest(
        @NotNull(message = "La plataforma es obligatoria")
        Long platformId,

        Long contentId,

        String authorName,

        @NotBlank(message = "El texto del comentario es obligatorio")
        String text,

        LocalDate postedAt
) {
}
