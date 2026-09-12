package com.marketinganalytics.platform.dto.comment;

import com.marketinganalytics.platform.entity.enums.CommentSentiment;

public record CommentFilter(
        Long platformId,
        CommentSentiment sentiment,
        String search
) {
}
