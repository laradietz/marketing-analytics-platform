package com.marketinganalytics.platform.service.impl;

import com.marketinganalytics.platform.dto.campaign.CampaignFilter;
import com.marketinganalytics.platform.dto.dashboard.DashboardFilter;
import com.marketinganalytics.platform.dto.dashboard.DashboardResponse;
import com.marketinganalytics.platform.dto.metric.MetricTotals;
import com.marketinganalytics.platform.entity.Campaign;
import com.marketinganalytics.platform.entity.CampaignMetric;
import com.marketinganalytics.platform.entity.Platform;
import com.marketinganalytics.platform.mapper.CampaignMetricMapper;
import com.marketinganalytics.platform.mapper.LeadMapper;
import com.marketinganalytics.platform.mapper.RecommendationMapper;
import com.marketinganalytics.platform.repository.CampaignMetricRepository;
import com.marketinganalytics.platform.repository.CampaignRepository;
import com.marketinganalytics.platform.repository.LeadRepository;
import com.marketinganalytics.platform.repository.RecommendationRepository;
import com.marketinganalytics.platform.repository.spec.CampaignSpecifications;
import com.marketinganalytics.platform.service.ActivityLogService;
import com.marketinganalytics.platform.service.DashboardService;
import com.marketinganalytics.platform.service.MetricsCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int DEFAULT_RANGE_DAYS = 30;

    private final CampaignRepository campaignRepository;
    private final CampaignMetricRepository campaignMetricRepository;
    private final LeadRepository leadRepository;
    private final RecommendationRepository recommendationRepository;
    private final ActivityLogService activityLogService;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(DashboardFilter filter) {
        LocalDate to = filter.to() != null ? filter.to() : LocalDate.now();
        LocalDate from = filter.from() != null ? filter.from() : to.minusDays(DEFAULT_RANGE_DAYS);

        List<Campaign> campaigns = campaignRepository
                .findAll(CampaignSpecifications.fromFilter(new CampaignFilter(null, null, filter.platformId(), filter.objective(), null)))
                .stream()
                .filter(c -> filter.campaignId() == null || filter.campaignId().equals(c.getId()))
                .toList();

        Map<Long, Campaign> campaignById = campaigns.stream().collect(Collectors.toMap(Campaign::getId, c -> c));
        List<Long> campaignIds = campaigns.stream().map(Campaign::getId).toList();

        List<CampaignMetric> metrics = campaignIds.isEmpty()
                ? List.of()
                : campaignMetricRepository.findByCampaignIdsBetween(campaignIds, from, to);

        MetricTotals kpiTotals = metrics.stream()
                .map(CampaignMetricMapper::toTotals)
                .reduce(MetricTotals.ZERO, MetricTotals::add);

        return new DashboardResponse(
                MetricsCalculationService.derive(kpiTotals),
                buildDailyBreakdown(metrics, from, to),
                buildPlatformPerformance(campaigns, metrics),
                buildCampaignRanking(campaignById, metrics, true),
                buildCampaignRanking(campaignById, metrics, false),
                buildBudgetDistribution(campaigns, metrics),
                leadRepository.findAllByOrderByCreatedAtDesc(org.springframework.data.domain.PageRequest.of(0, 5)).stream()
                        .map(LeadMapper::toResponse).toList(),
                recommendationRepository.findTop5ByOrderByCreatedAtDesc().stream()
                        .map(RecommendationMapper::toResponse).toList(),
                activityLogService.getRecent()
        );
    }

    private List<DashboardResponse.DailyBreakdownPoint> buildDailyBreakdown(List<CampaignMetric> metrics, LocalDate from, LocalDate to) {
        Map<LocalDate, MetricTotals> byDate = new TreeMap<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            byDate.put(d, MetricTotals.ZERO);
        }
        for (CampaignMetric m : metrics) {
            byDate.merge(m.getRecordedDate(), CampaignMetricMapper.toTotals(m), MetricTotals::add);
        }
        return byDate.entrySet().stream()
                .map(e -> new DashboardResponse.DailyBreakdownPoint(
                        e.getKey(), e.getValue().impressions(), e.getValue().clicks(), e.getValue().conversions(),
                        e.getValue().spend().setScale(2, RoundingMode.HALF_UP), e.getValue().revenue().setScale(2, RoundingMode.HALF_UP)))
                .toList();
    }

    private List<DashboardResponse.PlatformPerformance> buildPlatformPerformance(List<Campaign> campaigns, List<CampaignMetric> metrics) {
        Map<Long, List<CampaignMetric>> metricsByCampaign = metrics.stream()
                .collect(Collectors.groupingBy(m -> m.getCampaign().getId()));

        Map<Platform, MetricTotals> totalsByPlatform = new java.util.LinkedHashMap<>();
        for (Campaign campaign : campaigns) {
            MetricTotals totals = metricsByCampaign.getOrDefault(campaign.getId(), List.of()).stream()
                    .map(CampaignMetricMapper::toTotals)
                    .reduce(MetricTotals.ZERO, MetricTotals::add);
            totalsByPlatform.merge(campaign.getPlatform(), totals, MetricTotals::add);
        }

        return totalsByPlatform.entrySet().stream()
                .map(e -> {
                    var derived = MetricsCalculationService.derive(e.getValue());
                    Platform p = e.getKey();
                    return new DashboardResponse.PlatformPerformance(
                            p.getId(), p.getName(), p.getColorHex(),
                            e.getValue().spend(), e.getValue().revenue(), derived.roas(), derived.ctr(),
                            e.getValue().conversions());
                })
                .sorted(Comparator.comparing(DashboardResponse.PlatformPerformance::revenue).reversed())
                .toList();
    }

    private List<DashboardResponse.CampaignRanking> buildCampaignRanking(Map<Long, Campaign> campaignById,
                                                                          List<CampaignMetric> metrics, boolean top) {
        Map<Long, List<CampaignMetric>> metricsByCampaign = metrics.stream()
                .collect(Collectors.groupingBy(m -> m.getCampaign().getId()));

        List<DashboardResponse.CampaignRanking> rankings = campaignById.values().stream()
                .map(campaign -> {
                    MetricTotals totals = metricsByCampaign.getOrDefault(campaign.getId(), List.of()).stream()
                            .map(CampaignMetricMapper::toTotals)
                            .reduce(MetricTotals.ZERO, MetricTotals::add);
                    var derived = MetricsCalculationService.derive(totals);
                    return new DashboardResponse.CampaignRanking(
                            campaign.getId(), campaign.getName(), campaign.getPlatform().getName(),
                            derived.roas(), totals.spend(), totals.revenue());
                })
                .filter(r -> r.spend().compareTo(BigDecimal.ZERO) > 0)
                .sorted(top
                        ? Comparator.comparing(DashboardResponse.CampaignRanking::roas).reversed()
                        : Comparator.comparing(DashboardResponse.CampaignRanking::roas))
                .limit(5)
                .toList();

        return rankings;
    }

    private List<DashboardResponse.BudgetDistributionItem> buildBudgetDistribution(List<Campaign> campaigns, List<CampaignMetric> metrics) {
        Map<Long, List<CampaignMetric>> metricsByCampaign = metrics.stream()
                .collect(Collectors.groupingBy(m -> m.getCampaign().getId()));

        Map<Platform, BigDecimal> spendByPlatform = new java.util.LinkedHashMap<>();
        for (Campaign campaign : campaigns) {
            BigDecimal spend = metricsByCampaign.getOrDefault(campaign.getId(), List.of()).stream()
                    .map(CampaignMetric::getSpend)
                    .filter(java.util.Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            spendByPlatform.merge(campaign.getPlatform(), spend, BigDecimal::add);
        }

        BigDecimal totalSpend = spendByPlatform.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        return spendByPlatform.entrySet().stream()
                .filter(e -> e.getValue().compareTo(BigDecimal.ZERO) > 0)
                .map(e -> {
                    BigDecimal percentage = totalSpend.compareTo(BigDecimal.ZERO) > 0
                            ? e.getValue().divide(totalSpend, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return new DashboardResponse.BudgetDistributionItem(
                            e.getKey().getId(), e.getKey().getName(), e.getKey().getColorHex(), e.getValue(), percentage);
                })
                .sorted(Comparator.comparing(DashboardResponse.BudgetDistributionItem::spend).reversed())
                .toList();
    }
}
