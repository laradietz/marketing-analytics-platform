package com.marketinganalytics.platform.repository;

import com.marketinganalytics.platform.entity.Recommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    Page<Recommendation> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<Recommendation> findTop5ByOrderByCreatedAtDesc();
}
