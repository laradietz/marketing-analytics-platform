package com.marketinganalytics.platform.mapper;

import com.marketinganalytics.platform.dto.comment.CommentResponse;
import com.marketinganalytics.platform.entity.AudienceComment;

public final class AudienceCommentMapper {

    private AudienceCommentMapper() {
    }

    public static CommentResponse toResponse(AudienceComment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getPlatform().getId(),
                comment.getPlatform().getName(),
                comment.getContent() != null ? comment.getContent().getId() : null,
                comment.getContent() != null ? comment.getContent().getTitle() : null,
                comment.getAuthorName(),
                comment.getText(),
                comment.getSentiment(),
                comment.getSource(),
                comment.getPostedAt(),
                comment.getCreatedAt()
        );
    }
}
