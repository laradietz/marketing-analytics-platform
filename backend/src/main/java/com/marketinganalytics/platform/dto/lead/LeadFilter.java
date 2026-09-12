package com.marketinganalytics.platform.dto.lead;

import com.marketinganalytics.platform.entity.enums.LeadSource;
import com.marketinganalytics.platform.entity.enums.LeadStatus;

public record LeadFilter(
        String search,
        LeadStatus status,
        LeadSource source,
        Long campaignId
) {
}
