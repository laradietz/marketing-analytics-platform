package com.marketinganalytics.platform.dto.lead;

import com.marketinganalytics.platform.entity.enums.LeadSource;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LeadRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150)
        String name,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no es válido")
        String email,

        String phone,

        String company,

        @NotNull(message = "El origen es obligatorio")
        LeadSource source,

        Long campaignId,

        Long assignedToId,

        String notes
) {
}
