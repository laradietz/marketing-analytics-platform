package com.marketinganalytics.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * A single day's raw performance snapshot for a campaign, as it would be
 * pulled from an ad platform's reporting API (impressions, clicks, spend...).
 * Derived ratios (CTR, CPC, ROAS, ...) are computed on read, never stored,
 * so they can never drift out of sync with the raw counters.
 */
@Entity
@Table(name = "campaign_metrics", uniqueConstraints = {
        @UniqueConstraint(name = "uk_campaign_metric_date", columnNames = {"campaign_id", "recorded_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Column(name = "recorded_date", nullable = false)
    private LocalDate recordedDate;

    @Column(nullable = false)
    @Builder.Default
    private long impressions = 0;

    @Column(nullable = false)
    @Builder.Default
    private long reach = 0;

    @Column(nullable = false)
    @Builder.Default
    private long clicks = 0;

    @Column(nullable = false)
    @Builder.Default
    private long conversions = 0;

    @Column(nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal spend = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal revenue = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private long likes = 0;

    @Column(nullable = false)
    @Builder.Default
    private long comments = 0;

    @Column(nullable = false)
    @Builder.Default
    private long shares = 0;

    @Column(nullable = false)
    @Builder.Default
    private long saves = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
