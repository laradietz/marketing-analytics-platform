package com.marketinganalytics.platform.dto.integration;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SyncRequest(
        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate from,

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDate to
) {
}
