package com.marketinganalytics.platform.service.impl;

import com.marketinganalytics.platform.entity.enums.CampaignStatus;
import com.marketinganalytics.platform.entity.enums.RecommendationType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CampaignOptimizerEngineTest {

    @Test
    void recommendsBudgetIncreaseForClearlyOutperformingPlatform() {
        var instagram = new CampaignOptimizerEngine.PlatformAggregate(1L, "Instagram",
                new BigDecimal("100000"), new BigDecimal("420000"), new BigDecimal("4.2"));
        var facebook = new CampaignOptimizerEngine.PlatformAggregate(2L, "Facebook",
                new BigDecimal("80000"), new BigDecimal("136000"), new BigDecimal("1.7"));

        var recommendations = CampaignOptimizerEngine.analyzePlatforms(List.of(instagram, facebook));

        assertThat(recommendations)
                .anySatisfy(r -> {
                    assertThat(r.type()).isEqualTo(RecommendationType.BUDGET_INCREASE);
                    assertThat(r.platformId()).isEqualTo(1L);
                    assertThat(r.metricValue()).isEqualByComparingTo("4.2");
                });
    }

    @Test
    void recommendsBudgetDecreaseForPlatformLosingMoney() {
        var linkedin = new CampaignOptimizerEngine.PlatformAggregate(3L, "LinkedIn",
                new BigDecimal("50000"), new BigDecimal("45000"), new BigDecimal("0.9"));
        var google = new CampaignOptimizerEngine.PlatformAggregate(4L, "Google Ads",
                new BigDecimal("100000"), new BigDecimal("350000"), new BigDecimal("3.5"));

        var recommendations = CampaignOptimizerEngine.analyzePlatforms(List.of(linkedin, google));

        assertThat(recommendations)
                .anySatisfy(r -> {
                    assertThat(r.type()).isEqualTo(RecommendationType.BUDGET_DECREASE);
                    assertThat(r.platformId()).isEqualTo(3L);
                });
    }

    @Test
    void ignoresPlatformsBelowMinimumSpendThreshold() {
        var tiny = new CampaignOptimizerEngine.PlatformAggregate(5L, "TikTok",
                new BigDecimal("5"), new BigDecimal("1"), new BigDecimal("0.2"));

        var recommendations = CampaignOptimizerEngine.analyzePlatforms(List.of(tiny));

        assertThat(recommendations).isEmpty();
    }

    @Test
    void doesNotRecommendBudgetIncreaseWhenPlatformsPerformSimilarly() {
        var a = new CampaignOptimizerEngine.PlatformAggregate(1L, "A", new BigDecimal("100000"), new BigDecimal("310000"), new BigDecimal("3.1"));
        var b = new CampaignOptimizerEngine.PlatformAggregate(2L, "B", new BigDecimal("100000"), new BigDecimal("300000"), new BigDecimal("3.0"));

        var recommendations = CampaignOptimizerEngine.analyzePlatforms(List.of(a, b));

        assertThat(recommendations).noneMatch(r -> r.type() == RecommendationType.BUDGET_INCREASE);
    }

    @Test
    void recommendsPausingAnUnprofitableActiveCampaign() {
        var campaign = new CampaignOptimizerEngine.CampaignAggregate(10L, "Campaña X", CampaignStatus.ACTIVE,
                new BigDecimal("50000"), new BigDecimal("45000"), new BigDecimal("0.9"), new BigDecimal("2.0"));

        var recommendations = CampaignOptimizerEngine.analyzeCampaigns(List.of(campaign));

        assertThat(recommendations).anyMatch(r -> r.type() == RecommendationType.PAUSE_CAMPAIGN && r.campaignId().equals(10L));
    }

    @Test
    void recommendsCreativeRefreshForLowCtrCampaign() {
        var campaign = new CampaignOptimizerEngine.CampaignAggregate(11L, "Campaña Y", CampaignStatus.ACTIVE,
                new BigDecimal("50000"), new BigDecimal("200000"), new BigDecimal("4.0"), new BigDecimal("0.5"));

        var recommendations = CampaignOptimizerEngine.analyzeCampaigns(List.of(campaign));

        assertThat(recommendations).anyMatch(r -> r.type() == RecommendationType.CREATIVE_REFRESH && r.campaignId().equals(11L));
    }

    @Test
    void skipsDraftAndCompletedCampaignsRegardlessOfPerformance() {
        var draft = new CampaignOptimizerEngine.CampaignAggregate(12L, "Draft", CampaignStatus.DRAFT,
                new BigDecimal("50000"), new BigDecimal("1000"), new BigDecimal("0.02"), new BigDecimal("0.1"));

        var recommendations = CampaignOptimizerEngine.analyzeCampaigns(List.of(draft));

        assertThat(recommendations).isEmpty();
    }
}
