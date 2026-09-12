package com.marketinganalytics.platform.entity;

import com.marketinganalytics.platform.entity.enums.RecommendationPriority;
import com.marketinganalytics.platform.entity.enums.RecommendationStatus;
import com.marketinganalytics.platform.entity.enums.RecommendationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A data-driven suggestion produced by the campaign optimizer, e.g. "shift
 * budget towards Instagram" backed by a concrete metric/value pair so the
 * user can see exactly why it was raised.
 */
@Entity
@Table(name = "recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private RecommendationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RecommendationPriority priority;

    @Column(name = "related_metric_name", length = 40)
    private String relatedMetricName;

    @Column(name = "related_metric_value", precision = 14, scale = 4)
    private BigDecimal relatedMetricValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id")
    private Platform platform;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private RecommendationStatus status = RecommendationStatus.NEW;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
