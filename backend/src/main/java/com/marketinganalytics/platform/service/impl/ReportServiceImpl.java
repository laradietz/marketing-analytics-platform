package com.marketinganalytics.platform.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketinganalytics.platform.dto.common.PageResponse;
import com.marketinganalytics.platform.dto.metric.MetricTotals;
import com.marketinganalytics.platform.dto.recommendation.RecommendationResponse;
import com.marketinganalytics.platform.dto.report.ReportGenerateRequest;
import com.marketinganalytics.platform.dto.report.ReportResponse;
import com.marketinganalytics.platform.dto.report.ReportSummary;
import com.marketinganalytics.platform.entity.Campaign;
import com.marketinganalytics.platform.entity.CampaignMetric;
import com.marketinganalytics.platform.entity.Report;
import com.marketinganalytics.platform.exception.BadRequestException;
import com.marketinganalytics.platform.exception.ResourceNotFoundException;
import com.marketinganalytics.platform.mapper.CampaignMetricMapper;
import com.marketinganalytics.platform.mapper.RecommendationMapper;
import com.marketinganalytics.platform.repository.CampaignMetricRepository;
import com.marketinganalytics.platform.repository.CampaignRepository;
import com.marketinganalytics.platform.repository.RecommendationRepository;
import com.marketinganalytics.platform.repository.ReportRepository;
import com.marketinganalytics.platform.security.CurrentUserProvider;
import com.marketinganalytics.platform.service.ActivityLogService;
import com.marketinganalytics.platform.service.MetricsCalculationService;
import com.marketinganalytics.platform.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final CampaignRepository campaignRepository;
    private final CampaignMetricRepository campaignMetricRepository;
    private final RecommendationRepository recommendationRepository;
    private final ObjectMapper objectMapper;
    private final ActivityLogService activityLogService;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public ReportResponse generate(ReportGenerateRequest request) {
        if (request.periodEnd().isBefore(request.periodStart())) {
            throw new BadRequestException("El período de fin no puede ser anterior al de inicio");
        }

        List<Campaign> campaigns = campaignRepository.findAll().stream()
                .filter(c -> request.campaignIds() == null || request.campaignIds().isEmpty() || request.campaignIds().contains(c.getId()))
                .filter(c -> request.platformIds() == null || request.platformIds().isEmpty() || request.platformIds().contains(c.getPlatform().getId()))
                .toList();

        List<Long> campaignIds = campaigns.stream().map(Campaign::getId).toList();
        List<CampaignMetric> metrics = campaignIds.isEmpty() ? List.of()
                : campaignMetricRepository.findByCampaignIdsBetween(campaignIds, request.periodStart(), request.periodEnd());

        var metricsByCampaign = metrics.stream().collect(Collectors.groupingBy(m -> m.getCampaign().getId()));

        MetricTotals overallTotals = metrics.stream()
                .map(CampaignMetricMapper::toTotals)
                .reduce(MetricTotals.ZERO, MetricTotals::add);

        List<ReportSummary.CampaignBreakdown> breakdowns = campaigns.stream()
                .map(c -> {
                    MetricTotals totals = metricsByCampaign.getOrDefault(c.getId(), List.of()).stream()
                            .map(CampaignMetricMapper::toTotals)
                            .reduce(MetricTotals.ZERO, MetricTotals::add);
                    return new ReportSummary.CampaignBreakdown(
                            c.getId(), c.getName(), c.getPlatform().getName(), c.getStatus().name(),
                            MetricsCalculationService.derive(totals));
                })
                .toList();

        List<RecommendationResponse> recommendations = recommendationRepository.findTop5ByOrderByCreatedAtDesc().stream()
                .filter(r -> r.getCampaign() == null || campaignIds.contains(r.getCampaign().getId()))
                .map(RecommendationMapper::toResponse)
                .toList();

        ReportSummary summary = new ReportSummary(
                request.periodStart(), request.periodEnd(),
                MetricsCalculationService.derive(overallTotals),
                breakdowns, recommendations);

        Report report = Report.builder()
                .name(request.name())
                .periodStart(request.periodStart())
                .periodEnd(request.periodEnd())
                .campaignIds(joinIds(campaignIds))
                .platformIds(request.platformIds() != null ? joinIds(request.platformIds()) : "")
                .summaryJson(writeJson(summary))
                .generatedBy(currentUserProvider.getCurrentUser())
                .build();

        report = reportRepository.save(report);

        activityLogService.log(currentUserProvider.getCurrentUser(), "REPORT_GENERATED", "Report", report.getId(),
                "Se generó el reporte \"" + report.getName() + "\"");

        return toResponse(report, summary);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReportResponse> list(Pageable pageable) {
        return PageResponse.from(reportRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(r -> toResponse(r, readJson(r.getSummaryJson()))));
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponse getById(Long id) {
        Report report = findOrThrow(id);
        return toResponse(report, readJson(report.getSummaryJson()));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportCsv(Long id) {
        Report report = findOrThrow(id);
        ReportSummary summary = readJson(report.getSummaryJson());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8)) {
            writer.println("Reporte," + escape(report.getName()));
            writer.println("Periodo," + summary.periodStart() + " a " + summary.periodEnd());
            writer.println();
            writer.println("Inversion,Ingresos,ROAS,Conversiones,CTR,CPC,CPA");
            writer.println(String.join(",",
                    summary.kpis().totals().spend().toPlainString(),
                    summary.kpis().totals().revenue().toPlainString(),
                    summary.kpis().roas().toPlainString(),
                    String.valueOf(summary.kpis().totals().conversions()),
                    summary.kpis().ctr().toPlainString(),
                    summary.kpis().cpc().toPlainString(),
                    summary.kpis().cpa().toPlainString()));
            writer.println();
            writer.println("Campaña,Plataforma,Estado,Inversion,Ingresos,ROAS,CTR,Conversiones");
            for (var c : summary.campaigns()) {
                writer.println(String.join(",",
                        escape(c.campaignName()), escape(c.platformName()), c.status(),
                        c.metrics().totals().spend().toPlainString(),
                        c.metrics().totals().revenue().toPlainString(),
                        c.metrics().roas().toPlainString(),
                        c.metrics().ctr().toPlainString(),
                        String.valueOf(c.metrics().totals().conversions())));
            }
        }
        return out.toByteArray();
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.contains(",") ? "\"" + value.replace("\"", "\"\"") + "\"" : value;
    }

    private ReportResponse toResponse(Report report, ReportSummary summary) {
        return new ReportResponse(
                report.getId(), report.getName(), report.getPeriodStart(), report.getPeriodEnd(),
                report.getGeneratedBy() != null ? report.getGeneratedBy().getName() : null,
                summary, report.getCreatedAt());
    }

    private Report findOrThrow(Long id) {
        return reportRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Reporte", id));
    }

    private String joinIds(List<Long> ids) {
        return ids.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private String writeJson(ReportSummary summary) {
        try {
            return objectMapper.writeValueAsString(summary);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo serializar el resumen del reporte", e);
        }
    }

    private ReportSummary readJson(String json) {
        try {
            return objectMapper.readValue(json, ReportSummary.class);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo leer el resumen del reporte", e);
        }
    }
}
