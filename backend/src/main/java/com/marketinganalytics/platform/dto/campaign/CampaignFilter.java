package com.marketinganalytics.platform.dto.campaign;

import com.marketinganalytics.platform.entity.enums.CampaignObjective;
import com.marketinganalytics.platform.entity.enums.CampaignStatus;

public record CampaignFilter(
        String search,
        CampaignStatus status,
        Long platformId,
        CampaignObjective objective,
        Long ownerId
) {
}
