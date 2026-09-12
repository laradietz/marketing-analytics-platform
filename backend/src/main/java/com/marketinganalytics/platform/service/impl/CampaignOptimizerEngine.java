package com.marketinganalytics.platform.service.impl;

import com.marketinganalytics.platform.entity.enums.CampaignStatus;
import com.marketinganalytics.platform.entity.enums.RecommendationPriority;
import com.marketinganalytics.platform.entity.enums.RecommendationType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure, deterministic rule engine behind the Campaign Optimizer: turns
 * already-computed platform/campaign metric aggregates into recommendations.
 * No randomness — every suggestion is traceable back to a concrete metric
 * and threshold, which is why it takes plain aggregates rather than
 * repositories (keeps it trivially unit-testable).
 */
public final class CampaignOptimizerEngine {

    static final BigDecimal MIN_SPEND_FOR_ANALYSIS = BigDecimal.valueOf(50);
    static final BigDecimal ROAS_EXCELLENT = BigDecimal.valueOf(3.0);
    static final BigDecimal ROAS_POOR = BigDecimal.ONE;
    static final BigDecimal CTR_LOW_PERCENT = BigDecimal.valueOf(1.0);
    static final BigDecimal OUTPERFORM_MARGIN = BigDecimal.valueOf(1.2);

    private CampaignOptimizerEngine() {
    }

    public record PlatformAggregate(Long platformId, String platformName, BigDecimal spend, BigDecimal revenue, BigDecimal roas) {
    }

    public record CampaignAggregate(Long campaignId, String campaignName, CampaignStatus status,
                                     BigDecimal spend, BigDecimal revenue, BigDecimal roas,
                                     BigDecimal ctr) {
    }

    public record ProposedRecommendation(String title, String description, RecommendationType type,
                                          RecommendationPriority priority, String metricName, BigDecimal metricValue,
                                          Long campaignId, Long platformId) {
    }

    public static List<ProposedRecommendation> analyzePlatforms(List<PlatformAggregate> platforms) {
        List<ProposedRecommendation> results = new ArrayList<>();

        List<PlatformAggregate> qualifying = platforms.stream()
                .filter(p -> p.spend().compareTo(MIN_SPEND_FOR_ANALYSIS) >= 0)
                .toList();

        if (qualifying.size() >= 2) {
            PlatformAggregate best = qualifying.stream()
                    .max((a, b) -> a.roas().compareTo(b.roas()))
                    .orElseThrow();
            BigDecimal secondBestRoas = qualifying.stream()
                    .filter(p -> !p.platformId().equals(best.platformId()))
                    .map(PlatformAggregate::roas)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            boolean meaningfullyBetter = secondBestRoas.compareTo(BigDecimal.ZERO) == 0
                    || best.roas().compareTo(secondBestRoas.multiply(OUTPERFORM_MARGIN)) >= 0;

            if (best.roas().compareTo(ROAS_EXCELLENT) >= 0 && meaningfullyBetter) {
                results.add(new ProposedRecommendation(
                        best.platformName() + " presenta el mejor rendimiento actual",
                        best.platformName() + " tiene un ROAS de " + best.roas() +
                                " frente a un promedio de " + secondBestRoas + " en el resto de plataformas. " +
                                "Considerá evaluar un incremento gradual del presupuesto.",
                        RecommendationType.BUDGET_INCREASE,
                        RecommendationPriority.HIGH,
                        "ROAS", best.roas(), null, best.platformId()));
            }
        }

        for (PlatformAggregate platform : qualifying) {
            if (platform.roas().compareTo(ROAS_POOR) < 0) {
                results.add(new ProposedRecommendation(
                        platform.platformName() + " está operando a pérdida",
                        platform.platformName() + " tiene un ROAS de " + platform.roas() +
                                ", por debajo del punto de equilibrio (1.0). Considerá reducir el presupuesto " +
                                "asignado o revisar la segmentación y las creatividades utilizadas.",
                        RecommendationType.BUDGET_DECREASE,
                        RecommendationPriority.HIGH,
                        "ROAS", platform.roas(), null, platform.platformId()));
            }
        }

        return results;
    }

    public static List<ProposedRecommendation> analyzeCampaigns(List<CampaignAggregate> campaigns) {
        List<ProposedRecommendation> results = new ArrayList<>();

        for (CampaignAggregate campaign : campaigns) {
            if (campaign.status() != CampaignStatus.ACTIVE && campaign.status() != CampaignStatus.PAUSED) {
                continue;
            }
            if (campaign.spend().compareTo(MIN_SPEND_FOR_ANALYSIS) < 0) {
                continue;
            }

            if (campaign.roas().compareTo(ROAS_POOR) < 0) {
                results.add(new ProposedRecommendation(
                        "\"" + campaign.campaignName() + "\" no está siendo rentable",
                        "Esta campaña tiene un ROAS de " + campaign.roas() + ", generando menos ingresos que gasto. " +
                                "Evaluá pausarla o replantear su segmentación y creatividades.",
                        RecommendationType.PAUSE_CAMPAIGN,
                        RecommendationPriority.HIGH,
                        "ROAS", campaign.roas(), campaign.campaignId(), null));
            }

            if (campaign.ctr().compareTo(CTR_LOW_PERCENT) < 0) {
                results.add(new ProposedRecommendation(
                        "\"" + campaign.campaignName() + "\" tiene un CTR bajo",
                        "El CTR actual es de " + campaign.ctr() + "%, por debajo del 1% de referencia. " +
                                "Podría beneficiarse de una renovación creativa (nuevas imágenes, video o copy).",
                        RecommendationType.CREATIVE_REFRESH,
                        RecommendationPriority.MEDIUM,
                        "CTR", campaign.ctr(), campaign.campaignId(), null));
            }
        }

        return results;
    }
}
