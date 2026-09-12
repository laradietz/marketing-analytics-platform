package com.marketinganalytics.platform.mapper;

import com.marketinganalytics.platform.dto.content.ContentResponse;
import com.marketinganalytics.platform.entity.Content;

public final class ContentMapper {

    private ContentMapper() {
    }

    public static ContentResponse toResponse(Content content) {
        return new ContentResponse(
                content.getId(),
                content.getTitle(),
                content.getCopyText(),
                PlatformMapper.toResponse(content.getPlatform()),
                content.getObjective(),
                content.getStatus(),
                content.getScheduledDate(),
                content.getPublishedDate(),
                content.getHashtags(),
                content.getCtaText(),
                content.getCampaign() != null ? content.getCampaign().getId() : null,
                content.getCampaign() != null ? content.getCampaign().getName() : null,
                content.getCreatedBy() != null ? content.getCreatedBy().getName() : null,
                content.getCreatedAt(),
                content.getUpdatedAt()
        );
    }
}
