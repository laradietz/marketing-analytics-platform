package com.marketinganalytics.platform.entity;

import com.marketinganalytics.platform.entity.enums.AdFormat;
import com.marketinganalytics.platform.entity.enums.AdStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * An individual creative running inside a campaign. Campaigns typically run
 * several creative variants at once; splitting them out lets the optimizer
 * eventually reason about creative-level performance, not just campaign-level.
 */
@Entity
@Table(name = "ads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdFormat format;

    @Column(length = 200)
    private String headline;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "cta_label", length = 60)
    private String ctaLabel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AdStatus status = AdStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
