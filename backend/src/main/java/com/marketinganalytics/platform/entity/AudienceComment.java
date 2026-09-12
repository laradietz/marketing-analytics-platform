package com.marketinganalytics.platform.entity;

import com.marketinganalytics.platform.entity.enums.CommentSentiment;
import com.marketinganalytics.platform.entity.enums.CommentSource;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

/**
 * A comment an audience member left on a platform (on a specific
 * {@link Content} post when known, or general platform feedback otherwise).
 * Sentiment is computed automatically on save — see
 * {@code service.sentiment.SentimentClassifier} — and used to surface
 * recurring themes that feed the AI content generator with real audience
 * language instead of a guessed target-audience description.
 * <p>
 * Named {@code AudienceComment} (not {@code Comment}) to avoid clashing with
 * the unrelated concept of a code comment when scanning the codebase.
 */
@Entity
@Table(name = "audience_comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AudienceComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "platform_id", nullable = false)
    private Platform platform;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private Content content;

    @Column(name = "author_name", length = 150)
    private String authorName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CommentSentiment sentiment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private CommentSource source = CommentSource.MANUAL;

    @Column(name = "posted_at")
    private LocalDate postedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        if (postedAt == null) {
            postedAt = LocalDate.now();
        }
    }
}
