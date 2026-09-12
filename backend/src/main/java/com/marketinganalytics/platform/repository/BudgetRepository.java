package com.marketinganalytics.platform.repository;

import com.marketinganalytics.platform.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByCampaignIdOrderByPeriodStartDesc(Long campaignId);
}
