package com.marketinganalytics.platform.dto.comment;

import com.marketinganalytics.platform.entity.enums.CommentSentiment;
import com.marketinganalytics.platform.entity.enums.CommentSource;

import java.time.Instant;
import java.time.LocalDate;

public record CommentResponse(
        Long id,
        Long platformId,
        String platformName,
        Long contentId,
        String contentTitle,
        String authorName,
        String text,
        CommentSentiment sentiment,
        CommentSource source,
        LocalDate postedAt,
        Instant createdAt
) {
}
