package com.marketinganalytics.platform.dto.lead;

import com.marketinganalytics.platform.entity.enums.LeadStatus;
import jakarta.validation.constraints.NotNull;

public record LeadStatusUpdateRequest(
        @NotNull(message = "El estado es obligatorio")
        LeadStatus status
) {
}
