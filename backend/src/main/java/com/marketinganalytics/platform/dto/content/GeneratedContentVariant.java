package com.marketinganalytics.platform.dto.content;

import java.util.List;

/** One AI-generated content draft, before it is persisted as a {@code ContentIdea}. */
public record GeneratedContentVariant(
        String title,
        String copy,
        String callToAction,
        List<String> hashtags
) {
}
