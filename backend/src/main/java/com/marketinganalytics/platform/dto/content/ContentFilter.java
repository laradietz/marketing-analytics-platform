package com.marketinganalytics.platform.dto.content;

import com.marketinganalytics.platform.entity.enums.ContentStatus;

public record ContentFilter(
        ContentStatus status,
        Long platformId,
        Long campaignId
) {
}
