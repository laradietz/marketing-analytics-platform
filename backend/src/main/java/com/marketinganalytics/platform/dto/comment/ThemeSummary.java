package com.marketinganalytics.platform.dto.comment;

/** One recurring word/topic across a set of comments, with how often it appeared and a real example. */
public record ThemeSummary(String keyword, int mentions, String sampleQuote) {
}
