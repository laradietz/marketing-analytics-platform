package com.marketinganalytics.platform.repository;

import com.marketinganalytics.platform.entity.DailyMetric;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyMetricRepository extends JpaRepository<DailyMetric, Long> {
    Optional<DailyMetric> findByMetricDateAndPlatformId(LocalDate metricDate, Long platformId);
    List<DailyMetric> findByMetricDateBetweenOrderByMetricDateAsc(LocalDate from, LocalDate to);
}
