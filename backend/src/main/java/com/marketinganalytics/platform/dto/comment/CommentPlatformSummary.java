package com.marketinganalytics.platform.dto.comment;

import java.util.List;

public record CommentPlatformSummary(
        long total,
        long positive,
        long neutral,
        long negative,
        List<ThemeSummary> topThemes
) {
}
