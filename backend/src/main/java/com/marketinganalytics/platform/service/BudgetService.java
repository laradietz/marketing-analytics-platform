package com.marketinganalytics.platform.service;

import com.marketinganalytics.platform.dto.budget.BudgetRequest;
import com.marketinganalytics.platform.dto.budget.BudgetResponse;

import java.util.List;

public interface BudgetService {
    List<BudgetResponse> listByCampaign(Long campaignId);
    BudgetResponse create(Long campaignId, BudgetRequest request);
    void delete(Long campaignId, Long budgetId);
}
