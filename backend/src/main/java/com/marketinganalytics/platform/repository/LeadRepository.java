package com.marketinganalytics.platform.repository;

import com.marketinganalytics.platform.entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LeadRepository extends JpaRepository<Lead, Long>, JpaSpecificationExecutor<Lead> {
    List<Lead> findTop5ByOrderByCreatedAtDesc();
    List<Lead> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
