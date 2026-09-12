package com.marketinganalytics.platform.dto.integration;

import jakarta.validation.constraints.NotBlank;

public record ManualConnectRequest(
        @NotBlank(message = "El access token es obligatorio")
        String accessToken,

        String refreshToken,

        @NotBlank(message = "El ID de cuenta externa es obligatorio")
        String externalAccountId
) {
}
