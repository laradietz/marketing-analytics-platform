package com.marketinganalytics.platform.repository;

import com.marketinganalytics.platform.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
