package com.marketinganalytics.platform.service.impl;

import com.marketinganalytics.platform.dto.campaign.CampaignFilter;
import com.marketinganalytics.platform.dto.campaign.CampaignRequest;
import com.marketinganalytics.platform.dto.campaign.CampaignResponse;
import com.marketinganalytics.platform.dto.common.PageResponse;
import com.marketinganalytics.platform.dto.metric.DerivedMetrics;
import com.marketinganalytics.platform.dto.metric.MetricTotals;
import com.marketinganalytics.platform.entity.Campaign;
import com.marketinganalytics.platform.entity.CampaignMetric;
import com.marketinganalytics.platform.entity.Platform;
import com.marketinganalytics.platform.entity.User;
import com.marketinganalytics.platform.entity.enums.CampaignStatus;
import com.marketinganalytics.platform.exception.BadRequestException;
import com.marketinganalytics.platform.exception.ResourceNotFoundException;
import com.marketinganalytics.platform.mapper.CampaignMapper;
import com.marketinganalytics.platform.mapper.CampaignMetricMapper;
import com.marketinganalytics.platform.repository.CampaignMetricRepository;
import com.marketinganalytics.platform.repository.CampaignRepository;
import com.marketinganalytics.platform.repository.PlatformRepository;
import com.marketinganalytics.platform.repository.UserRepository;
import com.marketinganalytics.platform.repository.spec.CampaignSpecifications;
import com.marketinganalytics.platform.security.CurrentUserProvider;
import com.marketinganalytics.platform.service.ActivityLogService;
import com.marketinganalytics.platform.service.CampaignService;
import com.marketinganalytics.platform.service.MetricsCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CampaignServiceImpl implements CampaignService {

    private static final Map<CampaignStatus, Set<CampaignStatus>> ALLOWED_TRANSITIONS = Map.of(
            CampaignStatus.DRAFT, EnumSet.of(CampaignStatus.ACTIVE, CampaignStatus.CANCELLED),
            CampaignStatus.ACTIVE, EnumSet.of(CampaignStatus.PAUSED, CampaignStatus.COMPLETED, CampaignStatus.CANCELLED),
            CampaignStatus.PAUSED, EnumSet.of(CampaignStatus.ACTIVE, CampaignStatus.COMPLETED, CampaignStatus.CANCELLED),
            CampaignStatus.COMPLETED, EnumSet.noneOf(CampaignStatus.class),
            CampaignStatus.CANCELLED, EnumSet.noneOf(CampaignStatus.class)
    );

    private final CampaignRepository campaignRepository;
    private final PlatformRepository platformRepository;
    private final UserRepository userRepository;
    private final CampaignMetricRepository campaignMetricRepository;
    private final ActivityLogService activityLogService;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CampaignResponse> list(CampaignFilter filter, Pageable pageable) {
        Page<Campaign> page = campaignRepository.findAll(CampaignSpecifications.fromFilter(filter), pageable);
        return PageResponse.from(page.map(campaign -> CampaignMapper.toResponse(campaign, metricsFor(campaign))));
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignResponse getById(Long id) {
        Campaign campaign = findCampaignOrThrow(id);
        return CampaignMapper.toResponse(campaign, metricsFor(campaign));
    }

    @Override
    @Transactional
    public CampaignResponse create(CampaignRequest request) {
        Campaign campaign = new Campaign();
        applyRequest(campaign, request);
        campaign.setStatus(CampaignStatus.DRAFT);
        campaign = campaignRepository.save(campaign);

        activityLogService.log(currentUserProvider.getCurrentUser(), "CAMPAIGN_CREATED", "Campaign", campaign.getId(),
                "Se creó la campaña \"" + campaign.getName() + "\"");

        return CampaignMapper.toResponse(campaign, metricsFor(campaign));
    }

    @Override
    @Transactional
    public CampaignResponse update(Long id, CampaignRequest request) {
        Campaign campaign = findCampaignOrThrow(id);
        applyRequest(campaign, request);
        campaign = campaignRepository.save(campaign);

        activityLogService.log(currentUserProvider.getCurrentUser(), "CAMPAIGN_UPDATED", "Campaign", campaign.getId(),
                "Se actualizó la campaña \"" + campaign.getName() + "\"");

        return CampaignMapper.toResponse(campaign, metricsFor(campaign));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Campaign campaign = findCampaignOrThrow(id);
        campaignRepository.delete(campaign);
        activityLogService.log(currentUserProvider.getCurrentUser(), "CAMPAIGN_DELETED", "Campaign", id,
                "Se eliminó la campaña \"" + campaign.getName() + "\"");
    }

    @Override
    @Transactional
    public CampaignResponse activate(Long id) {
        return transitionTo(id, CampaignStatus.ACTIVE);
    }

    @Override
    @Transactional
    public CampaignResponse pause(Long id) {
        return transitionTo(id, CampaignStatus.PAUSED);
    }

    @Override
    @Transactional
    public CampaignResponse complete(Long id) {
        return transitionTo(id, CampaignStatus.COMPLETED);
    }

    @Override
    @Transactional
    public CampaignResponse cancel(Long id) {
        return transitionTo(id, CampaignStatus.CANCELLED);
    }

    private CampaignResponse transitionTo(Long id, CampaignStatus target) {
        Campaign campaign = findCampaignOrThrow(id);
        Set<CampaignStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(campaign.getStatus(), Set.of());
        if (!allowed.contains(target)) {
            throw new BadRequestException(
                    "No se puede pasar la campaña de " + campaign.getStatus() + " a " + target);
        }
        campaign.setStatus(target);
        campaign = campaignRepository.save(campaign);

        activityLogService.log(currentUserProvider.getCurrentUser(), "CAMPAIGN_STATUS_CHANGED", "Campaign", campaign.getId(),
                "La campaña \"" + campaign.getName() + "\" pasó a estado " + target);

        return CampaignMapper.toResponse(campaign, metricsFor(campaign));
    }

    private void applyRequest(Campaign campaign, CampaignRequest request) {
        Platform platform = platformRepository.findById(request.platformId())
                .orElseThrow(() -> ResourceNotFoundException.of("Plataforma", request.platformId()));

        if (request.endDate() != null && request.endDate().isBefore(request.startDate())) {
            throw new BadRequestException("La fecha de finalización no puede ser anterior a la de inicio");
        }

        campaign.setName(request.name());
        campaign.setDescription(request.description());
        campaign.setPlatform(platform);
        campaign.setObjective(request.objective());
        campaign.setBudget(request.budget());
        campaign.setStartDate(request.startDate());
        campaign.setEndDate(request.endDate());
        campaign.setTargetAudience(request.targetAudience());
        campaign.setNotes(request.notes());
        campaign.setExternalCampaignId(request.externalCampaignId());

        if (request.ownerId() != null) {
            User owner = userRepository.findById(request.ownerId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Usuario", request.ownerId()));
            campaign.setOwner(owner);
        } else if (campaign.getOwner() == null) {
            campaign.setOwner(currentUserProvider.getCurrentUser());
        }
    }

    private Campaign findCampaignOrThrow(Long id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Campaña", id));
    }

    private DerivedMetrics metricsFor(Campaign campaign) {
        List<CampaignMetric> metrics = campaignMetricRepository.findByCampaignIdOrderByRecordedDateAsc(campaign.getId());
        MetricTotals totals = metrics.stream()
                .map(CampaignMetricMapper::toTotals)
                .reduce(MetricTotals.ZERO, MetricTotals::add);
        return MetricsCalculationService.derive(totals);
    }
}
