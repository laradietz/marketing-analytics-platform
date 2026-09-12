package com.marketinganalytics.platform.service.impl;

import com.marketinganalytics.platform.dto.common.PageResponse;
import com.marketinganalytics.platform.dto.metric.MetricTotals;
import com.marketinganalytics.platform.dto.recommendation.RecommendationResponse;
import com.marketinganalytics.platform.entity.Campaign;
import com.marketinganalytics.platform.entity.CampaignMetric;
import com.marketinganalytics.platform.entity.Platform;
import com.marketinganalytics.platform.entity.Recommendation;
import com.marketinganalytics.platform.entity.enums.RecommendationStatus;
import com.marketinganalytics.platform.exception.ResourceNotFoundException;
import com.marketinganalytics.platform.mapper.CampaignMetricMapper;
import com.marketinganalytics.platform.mapper.RecommendationMapper;
import com.marketinganalytics.platform.repository.CampaignMetricRepository;
import com.marketinganalytics.platform.repository.CampaignRepository;
import com.marketinganalytics.platform.repository.PlatformRepository;
import com.marketinganalytics.platform.repository.RecommendationRepository;
import com.marketinganalytics.platform.security.CurrentUserProvider;
import com.marketinganalytics.platform.service.ActivityLogService;
import com.marketinganalytics.platform.service.MetricsCalculationService;
import com.marketinganalytics.platform.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final CampaignRepository campaignRepository;
    private final PlatformRepository platformRepository;
    private final CampaignMetricRepository campaignMetricRepository;
    private final RecommendationRepository recommendationRepository;
    private final ActivityLogService activityLogService;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public List<RecommendationResponse> generate() {
        List<Campaign> campaigns = campaignRepository.findAll();
        Map<Long, List<CampaignMetric>> metricsByCampaign = campaigns.stream()
                .collect(Collectors.toMap(Campaign::getId,
                        c -> campaignMetricRepository.findByCampaignIdOrderByRecordedDateAsc(c.getId())));

        List<CampaignOptimizerEngine.CampaignAggregate> campaignAggregates = new ArrayList<>();
        Map<Long, MetricTotals> totalsByPlatform = new java.util.HashMap<>();
        Map<Long, Platform> platformById = new java.util.HashMap<>();

        for (Campaign campaign : campaigns) {
            MetricTotals totals = metricsByCampaign.get(campaign.getId()).stream()
                    .map(CampaignMetricMapper::toTotals)
                    .reduce(MetricTotals.ZERO, MetricTotals::add);

            var derived = MetricsCalculationService.derive(totals);
            campaignAggregates.add(new CampaignOptimizerEngine.CampaignAggregate(
                    campaign.getId(), campaign.getName(), campaign.getStatus(),
                    totals.spend(), totals.revenue(), derived.roas(), derived.ctr()));

            Platform platform = campaign.getPlatform();
            platformById.put(platform.getId(), platform);
            totalsByPlatform.merge(platform.getId(), totals, MetricTotals::add);
        }

        List<CampaignOptimizerEngine.PlatformAggregate> platformAggregates = totalsByPlatform.entrySet().stream()
                .map(entry -> {
                    Platform platform = platformById.get(entry.getKey());
                    var derived = MetricsCalculationService.derive(entry.getValue());
                    return new CampaignOptimizerEngine.PlatformAggregate(
                            platform.getId(), platform.getName(), entry.getValue().spend(), entry.getValue().revenue(), derived.roas());
                })
                .toList();

        List<CampaignOptimizerEngine.ProposedRecommendation> proposed = new ArrayList<>();
        proposed.addAll(CampaignOptimizerEngine.analyzePlatforms(platformAggregates));
        proposed.addAll(CampaignOptimizerEngine.analyzeCampaigns(campaignAggregates));

        // Recommendations are re-derived from live data on every run, so stale
        // NEW suggestions from a previous analysis are superseded rather than
        // left to accumulate indefinitely.
        recommendationRepository.deleteAll(
                recommendationRepository.findAll().stream()
                        .filter(r -> r.getStatus() == RecommendationStatus.NEW)
                        .toList());

        Map<Long, Campaign> campaignById = campaigns.stream().collect(Collectors.toMap(Campaign::getId, c -> c));

        List<Recommendation> entities = proposed.stream().map(p -> Recommendation.builder()
                .title(p.title())
                .description(p.description())
                .type(p.type())
                .priority(p.priority())
                .relatedMetricName(p.metricName())
                .relatedMetricValue(p.metricValue())
                .campaign(p.campaignId() != null ? campaignById.get(p.campaignId()) : null)
                .platform(p.platformId() != null ? platformById.get(p.platformId()) : null)
                .status(RecommendationStatus.NEW)
                .build()
        ).toList();

        List<Recommendation> saved = recommendationRepository.saveAll(entities);

        activityLogService.log(currentUserProvider.getCurrentUser(), "RECOMMENDATIONS_GENERATED", "Recommendation", null,
                "Se generaron " + saved.size() + " recomendaciones a partir del análisis de campañas");

        return saved.stream().map(RecommendationMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RecommendationResponse> list(Pageable pageable) {
        return PageResponse.from(recommendationRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(RecommendationMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecommendationResponse> getRecent() {
        return recommendationRepository.findTop5ByOrderByCreatedAtDesc().stream()
                .map(RecommendationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public RecommendationResponse updateStatus(Long id, RecommendationStatus status) {
        Recommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Recomendación", id));
        recommendation.setStatus(status);
        return RecommendationMapper.toResponse(recommendationRepository.save(recommendation));
    }
}
