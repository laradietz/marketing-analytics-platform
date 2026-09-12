package com.marketinganalytics.platform.service.impl;

import com.marketinganalytics.platform.dto.metric.CampaignMetricRequest;
import com.marketinganalytics.platform.dto.metric.CampaignMetricResponse;
import com.marketinganalytics.platform.dto.metric.MetricImportResult;
import com.marketinganalytics.platform.entity.Campaign;
import com.marketinganalytics.platform.entity.CampaignMetric;
import com.marketinganalytics.platform.entity.DailyMetric;
import com.marketinganalytics.platform.exception.BadRequestException;
import com.marketinganalytics.platform.exception.ResourceNotFoundException;
import com.marketinganalytics.platform.mapper.CampaignMetricMapper;
import com.marketinganalytics.platform.repository.CampaignMetricRepository;
import com.marketinganalytics.platform.repository.CampaignRepository;
import com.marketinganalytics.platform.repository.DailyMetricRepository;
import com.marketinganalytics.platform.security.CurrentUserProvider;
import com.marketinganalytics.platform.service.ActivityLogService;
import com.marketinganalytics.platform.service.CampaignMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CampaignMetricServiceImpl implements CampaignMetricService {

    private final CampaignMetricRepository campaignMetricRepository;
    private final CampaignRepository campaignRepository;
    private final DailyMetricRepository dailyMetricRepository;
    private final ActivityLogService activityLogService;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional(readOnly = true)
    public List<CampaignMetricResponse> listByCampaign(Long campaignId) {
        ensureCampaignExists(campaignId);
        return campaignMetricRepository.findByCampaignIdOrderByRecordedDateAsc(campaignId).stream()
                .map(CampaignMetricMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CampaignMetricResponse record(Long campaignId, CampaignMetricRequest request) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> ResourceNotFoundException.of("Campaña", campaignId));

        CampaignMetric metric = upsertMetric(campaign, request);

        activityLogService.log(currentUserProvider.getCurrentUser(), "METRIC_RECORDED", "Campaign", campaignId,
                "Se registraron métricas del " + request.recordedDate() + " para \"" + campaign.getName() + "\"");

        return CampaignMetricMapper.toResponse(metric);
    }

    @Override
    @Transactional
    public MetricImportResult importCsv(Long campaignId, MultipartFile file) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> ResourceNotFoundException.of("Campaña", campaignId));

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("El archivo está vacío");
        }

        String content;
        try {
            content = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new BadRequestException("No se pudo leer el archivo");
        }

        MetricCsvParser.ParseOutcome outcome = MetricCsvParser.parse(content);
        List<MetricImportResult.RowError> errors = new ArrayList<>(outcome.errors());

        int imported = 0;
        for (CampaignMetricRequest request : outcome.rows()) {
            try {
                upsertMetric(campaign, request);
                imported++;
            } catch (Exception ex) {
                errors.add(new MetricImportResult.RowError(-1, request.recordedDate() + ": " + ex.getMessage()));
            }
        }

        activityLogService.log(currentUserProvider.getCurrentUser(), "METRICS_IMPORTED", "Campaign", campaignId,
                "Se importaron " + imported + " filas de métricas para \"" + campaign.getName() + "\" desde CSV");

        return new MetricImportResult(outcome.totalRows(), imported, errors);
    }

    private CampaignMetric upsertMetric(Campaign campaign, CampaignMetricRequest request) {
        CampaignMetric metric = campaignMetricRepository
                .findByCampaignIdAndRecordedDate(campaign.getId(), request.recordedDate())
                .orElseGet(() -> CampaignMetric.builder().campaign(campaign).recordedDate(request.recordedDate()).build());

        metric.setImpressions(request.impressions());
        metric.setReach(request.reach());
        metric.setClicks(request.clicks());
        metric.setConversions(request.conversions());
        metric.setSpend(request.spend());
        metric.setRevenue(request.revenue());
        metric.setLikes(request.likes() != null ? request.likes() : 0);
        metric.setComments(request.comments() != null ? request.comments() : 0);
        metric.setShares(request.shares() != null ? request.shares() : 0);
        metric.setSaves(request.saves() != null ? request.saves() : 0);

        metric = campaignMetricRepository.save(metric);

        refreshDailyRollup(campaign.getPlatform().getId(), request.recordedDate());

        return metric;
    }

    private void refreshDailyRollup(Long platformId, java.time.LocalDate date) {
        List<CampaignMetric> dayMetrics = campaignMetricRepository.findByPlatformIdAndRecordedDate(platformId, date);

        long impressions = 0;
        long clicks = 0;
        long conversions = 0;
        BigDecimal spend = BigDecimal.ZERO;
        BigDecimal revenue = BigDecimal.ZERO;

        for (CampaignMetric m : dayMetrics) {
            impressions += m.getImpressions();
            clicks += m.getClicks();
            conversions += m.getConversions();
            spend = spend.add(m.getSpend() != null ? m.getSpend() : BigDecimal.ZERO);
            revenue = revenue.add(m.getRevenue() != null ? m.getRevenue() : BigDecimal.ZERO);
        }

        DailyMetric rollup = dailyMetricRepository.findByMetricDateAndPlatformId(date, platformId)
                .orElseGet(() -> DailyMetric.builder()
                        .metricDate(date)
                        .platform(dayMetrics.get(0).getCampaign().getPlatform())
                        .build());

        rollup.setImpressions(impressions);
        rollup.setClicks(clicks);
        rollup.setConversions(conversions);
        rollup.setSpend(spend);
        rollup.setRevenue(revenue);

        dailyMetricRepository.save(rollup);
    }

    private void ensureCampaignExists(Long campaignId) {
        if (!campaignRepository.existsById(campaignId)) {
            throw ResourceNotFoundException.of("Campaña", campaignId);
        }
    }
}
