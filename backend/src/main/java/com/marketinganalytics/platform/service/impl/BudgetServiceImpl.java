package com.marketinganalytics.platform.service.impl;

import com.marketinganalytics.platform.dto.budget.BudgetRequest;
import com.marketinganalytics.platform.dto.budget.BudgetResponse;
import com.marketinganalytics.platform.entity.Budget;
import com.marketinganalytics.platform.entity.Campaign;
import com.marketinganalytics.platform.entity.CampaignMetric;
import com.marketinganalytics.platform.exception.BadRequestException;
import com.marketinganalytics.platform.exception.ResourceNotFoundException;
import com.marketinganalytics.platform.repository.BudgetRepository;
import com.marketinganalytics.platform.repository.CampaignMetricRepository;
import com.marketinganalytics.platform.repository.CampaignRepository;
import com.marketinganalytics.platform.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final CampaignRepository campaignRepository;
    private final CampaignMetricRepository campaignMetricRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponse> listByCampaign(Long campaignId) {
        return budgetRepository.findByCampaignIdOrderByPeriodStartDesc(campaignId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public BudgetResponse create(Long campaignId, BudgetRequest request) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> ResourceNotFoundException.of("Campaña", campaignId));

        if (request.periodEnd().isBefore(request.periodStart())) {
            throw new BadRequestException("El período de fin no puede ser anterior al de inicio");
        }

        Budget budget = Budget.builder()
                .campaign(campaign)
                .periodStart(request.periodStart())
                .periodEnd(request.periodEnd())
                .plannedAmount(request.plannedAmount())
                .notes(request.notes())
                .build();

        return toResponse(budgetRepository.save(budget));
    }

    @Override
    @Transactional
    public void delete(Long campaignId, Long budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> ResourceNotFoundException.of("Presupuesto", budgetId));
        if (!budget.getCampaign().getId().equals(campaignId)) {
            throw new BadRequestException("El presupuesto no pertenece a la campaña indicada");
        }
        budgetRepository.delete(budget);
    }

    private BudgetResponse toResponse(Budget budget) {
        BigDecimal actualSpend = campaignMetricRepository.findByCampaignIdOrderByRecordedDateAsc(budget.getCampaign().getId())
                .stream()
                .filter(m -> !m.getRecordedDate().isBefore(budget.getPeriodStart()) && !m.getRecordedDate().isAfter(budget.getPeriodEnd()))
                .map(CampaignMetric::getSpend)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new BudgetResponse(
                budget.getId(), budget.getCampaign().getId(), budget.getPeriodStart(), budget.getPeriodEnd(),
                budget.getPlannedAmount(), actualSpend, budget.getNotes());
    }
}
