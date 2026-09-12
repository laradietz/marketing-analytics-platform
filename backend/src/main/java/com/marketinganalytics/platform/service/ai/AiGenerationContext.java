package com.marketinganalytics.platform.service.ai;

import com.marketinganalytics.platform.entity.enums.CampaignObjective;
import com.marketinganalytics.platform.entity.enums.ContentTone;

public record AiGenerationContext(
        String productOrService,
        String targetAudience,
        String platformName,
        CampaignObjective objective,
        ContentTone tone,
        String callToAction,
        int variantCount
) {
}
