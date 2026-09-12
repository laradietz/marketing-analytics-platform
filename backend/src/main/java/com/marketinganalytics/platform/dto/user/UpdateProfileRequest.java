package com.marketinganalytics.platform.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120)
        String name,

        String jobTitle,

        String companyName
) {
}
