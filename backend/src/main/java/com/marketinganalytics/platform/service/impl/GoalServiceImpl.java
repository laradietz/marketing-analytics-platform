package com.marketinganalytics.platform.service.impl;

import com.marketinganalytics.platform.dto.goal.GoalRequest;
import com.marketinganalytics.platform.dto.goal.GoalResponse;
import com.marketinganalytics.platform.dto.metric.MetricTotals;
import com.marketinganalytics.platform.entity.Campaign;
import com.marketinganalytics.platform.entity.CampaignMetric;
import com.marketinganalytics.platform.entity.Goal;
import com.marketinganalytics.platform.exception.ResourceNotFoundException;
import com.marketinganalytics.platform.mapper.CampaignMetricMapper;
import com.marketinganalytics.platform.repository.CampaignMetricRepository;
import com.marketinganalytics.platform.repository.CampaignRepository;
import com.marketinganalytics.platform.repository.GoalRepository;
import com.marketinganalytics.platform.repository.LeadRepository;
import com.marketinganalytics.platform.security.CurrentUserProvider;
import com.marketinganalytics.platform.service.GoalService;
import com.marketinganalytics.platform.service.MetricsCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final CampaignRepository campaignRepository;
    private final CampaignMetricRepository campaignMetricRepository;
    private final LeadRepository leadRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional(readOnly = true)
    public List<GoalResponse> list() {
        return goalRepository.findAllByOrderByPeriodEndAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public GoalResponse create(GoalRequest request) {
        Goal goal = Goal.builder()
                .name(request.name())
                .metricType(request.metricType())
                .targetValue(request.targetValue())
                .periodStart(request.periodStart())
                .periodEnd(request.periodEnd())
                .createdBy(currentUserProvider.getCurrentUser())
                .build();

        if (request.campaignId() != null) {
            Campaign campaign = campaignRepository.findById(request.campaignId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Campaña", request.campaignId()));
            goal.setCampaign(campaign);
        }

        return toResponse(goalRepository.save(goal));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!goalRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Objetivo", id);
        }
        goalRepository.deleteById(id);
    }

    private GoalResponse toResponse(Goal goal) {
        BigDecimal currentValue = computeCurrentValue(goal);
        BigDecimal progress = goal.getTargetValue().compareTo(BigDecimal.ZERO) > 0
                ? currentValue.divide(goal.getTargetValue(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new GoalResponse(
                goal.getId(), goal.getName(), goal.getMetricType(), goal.getTargetValue(), currentValue,
                progress, goal.getPeriodStart(), goal.getPeriodEnd(),
                goal.getCampaign() != null ? goal.getCampaign().getId() : null,
                goal.getCampaign() != null ? goal.getCampaign().getName() : null);
    }

    private BigDecimal computeCurrentValue(Goal goal) {
        if (goal.getMetricType() == com.marketinganalytics.platform.entity.enums.GoalMetric.LEADS) {
            return BigDecimal.valueOf(leadRepository.findAll().stream()
                    .filter(l -> !l.getCreatedAt().isBefore(goal.getPeriodStart().atStartOfDay(ZoneOffset.UTC).toInstant()))
                    .filter(l -> !l.getCreatedAt().isAfter(goal.getPeriodEnd().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()))
                    .filter(l -> goal.getCampaign() == null || (l.getCampaign() != null && l.getCampaign().getId().equals(goal.getCampaign().getId())))
                    .count());
        }

        List<Long> campaignIds = goal.getCampaign() != null
                ? List.of(goal.getCampaign().getId())
                : campaignRepository.findAll().stream().map(Campaign::getId).toList();

        List<CampaignMetric> metrics = campaignIds.isEmpty()
                ? List.of()
                : campaignMetricRepository.findByCampaignIdsBetween(campaignIds, goal.getPeriodStart(), goal.getPeriodEnd());

        MetricTotals totals = metrics.stream().map(CampaignMetricMapper::toTotals).reduce(MetricTotals.ZERO, MetricTotals::add);
        var derived = MetricsCalculationService.derive(totals);

        return switch (goal.getMetricType()) {
            case CONVERSIONS -> BigDecimal.valueOf(totals.conversions());
            case REVENUE -> totals.revenue();
            case ROAS -> derived.roas();
            case CTR -> derived.ctr();
            case LEADS -> BigDecimal.ZERO;
        };
    }
}
