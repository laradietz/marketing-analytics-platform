package com.marketinganalytics.platform.entity;

import com.marketinganalytics.platform.entity.enums.CampaignObjective;
import com.marketinganalytics.platform.entity.enums.ContentIdeaStatus;
import com.marketinganalytics.platform.entity.enums.ContentTone;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * A single AI-generated content draft (title/copy/CTA/hashtags) produced by
 * the content generator for a given product, audience, platform and tone.
 * Kept separate from {@link Content} so a user can generate several variants,
 * compare them, and only promote the chosen one onto the content calendar.
 */
@Entity
@Table(name = "content_ideas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentIdea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_or_service", nullable = false, length = 200)
    private String productOrService;

    @Column(name = "target_audience", length = 300)
    private String targetAudience;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id")
    private Platform platform;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CampaignObjective objective;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContentTone tone;

    @Column(name = "generated_title", length = 200)
    private String generatedTitle;

    @Column(name = "generated_copy", columnDefinition = "TEXT")
    private String generatedCopy;

    @Column(name = "generated_cta", length = 150)
    private String generatedCta;

    @Column(name = "generated_hashtags", columnDefinition = "TEXT")
    private String generatedHashtags;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    @Builder.Default
    private ContentIdeaStatus status = ContentIdeaStatus.SAVED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
