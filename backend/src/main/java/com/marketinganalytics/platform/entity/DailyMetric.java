package com.marketinganalytics.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Pre-aggregated, per-platform-per-day rollup of every {@link CampaignMetric}
 * recorded that day. Maintained incrementally by the metrics service so the
 * dashboard's trend charts can read a handful of rows instead of aggregating
 * the full campaign_metrics table on every request.
 */
@Entity
@Table(name = "daily_metrics", uniqueConstraints = {
        @UniqueConstraint(name = "uk_daily_metric_date_platform", columnNames = {"metric_date", "platform_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "platform_id", nullable = false)
    private Platform platform;

    @Column(nullable = false)
    @Builder.Default
    private long impressions = 0;

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
}
