package com.marketinganalytics.platform.dto.content;

import com.marketinganalytics.platform.dto.platform.PlatformResponse;
import com.marketinganalytics.platform.entity.enums.CampaignObjective;
import com.marketinganalytics.platform.entity.enums.ContentIdeaStatus;
import com.marketinganalytics.platform.entity.enums.ContentTone;

import java.time.Instant;

public record ContentIdeaResponse(
        Long id,
        String productOrService,
        String targetAudience,
        PlatformResponse platform,
        CampaignObjective objective,
        ContentTone tone,
        String generatedTitle,
        String generatedCopy,
        String generatedCta,
        String generatedHashtags,
        ContentIdeaStatus status,
        Instant createdAt
) {
}
