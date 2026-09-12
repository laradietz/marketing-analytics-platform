package com.marketinganalytics.platform.service.sentiment;

import com.marketinganalytics.platform.entity.enums.CommentSentiment;

/**
 * Classifies a comment's sentiment. The bundled {@link KeywordSentimentClassifier}
 * is a deterministic, keyword-based rule engine — accurate enough for
 * surfacing themes without any external dependency or API key. To swap in a
 * real NLP/AI sentiment service later, implement this interface, annotate it
 * {@code @Service} and {@code @Primary}, and read its credentials from an
 * environment variable — nothing else in {@code AudienceCommentService} needs
 * to change.
 */
public interface SentimentClassifier {
    CommentSentiment classify(String text);
}
