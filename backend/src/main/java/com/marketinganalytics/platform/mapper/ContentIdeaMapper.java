package com.marketinganalytics.platform.mapper;

import com.marketinganalytics.platform.dto.content.ContentIdeaResponse;
import com.marketinganalytics.platform.entity.ContentIdea;

public final class ContentIdeaMapper {

    private ContentIdeaMapper() {
    }

    public static ContentIdeaResponse toResponse(ContentIdea idea) {
        return new ContentIdeaResponse(
                idea.getId(),
                idea.getProductOrService(),
                idea.getTargetAudience(),
                idea.getPlatform() != null ? PlatformMapper.toResponse(idea.getPlatform()) : null,
                idea.getObjective(),
                idea.getTone(),
                idea.getGeneratedTitle(),
                idea.getGeneratedCopy(),
                idea.getGeneratedCta(),
                idea.getGeneratedHashtags(),
                idea.getStatus(),
                idea.getCreatedAt()
        );
    }
}
