package com.marketinganalytics.platform.dto.lead;

import com.marketinganalytics.platform.entity.enums.LeadSource;
import com.marketinganalytics.platform.entity.enums.LeadStatus;
import com.marketinganalytics.platform.entity.enums.LeadTemperature;

import java.time.Instant;

public record LeadResponse(
        Long id,
        String name,
        String email,
        String phone,
        String company,
        LeadSource source,
        LeadStatus status,
        int score,
        int contactCount,
        LeadTemperature temperature,
        Long campaignId,
        String campaignName,
        Long assignedToId,
        String assignedToName,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}
