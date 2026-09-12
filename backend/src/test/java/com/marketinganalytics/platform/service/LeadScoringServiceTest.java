package com.marketinganalytics.platform.service;

import com.marketinganalytics.platform.entity.Campaign;
import com.marketinganalytics.platform.entity.Lead;
import com.marketinganalytics.platform.entity.enums.CampaignObjective;
import com.marketinganalytics.platform.entity.enums.LeadSource;
import com.marketinganalytics.platform.entity.enums.LeadStatus;
import com.marketinganalytics.platform.entity.enums.LeadTemperature;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LeadScoringServiceTest {

    @Test
    void newLeadFromWeakSourceScoresLowAndCold() {
        Lead lead = Lead.builder().source(LeadSource.OTHER).status(LeadStatus.NEW).contactCount(0).build();

        int score = LeadScoringService.computeScore(lead);

        assertThat(score).isLessThan(40);
        assertThat(LeadScoringService.temperatureFor(score, lead)).isEqualTo(LeadTemperature.COLD);
    }

    @Test
    void convertedReferralWithConversionsCampaignScoresHotAndCapsAt100() {
        Campaign campaign = Campaign.builder().objective(CampaignObjective.CONVERSIONS).build();
        Lead lead = Lead.builder()
                .source(LeadSource.REFERRAL)
                .status(LeadStatus.CONVERTED)
                .contactCount(10)
                .campaign(campaign)
                .build();

        int score = LeadScoringService.computeScore(lead);

        assertThat(score).isEqualTo(100);
        assertThat(LeadScoringService.temperatureFor(score, lead)).isEqualTo(LeadTemperature.HOT);
    }

    @Test
    void lostLeadIsAlwaysColdRegardlessOfScore() {
        Campaign campaign = Campaign.builder().objective(CampaignObjective.SALES).build();
        Lead lead = Lead.builder()
                .source(LeadSource.REFERRAL)
                .status(LeadStatus.LOST)
                .contactCount(5)
                .campaign(campaign)
                .build();

        int score = LeadScoringService.computeScore(lead);

        assertThat(LeadScoringService.temperatureFor(score, lead)).isEqualTo(LeadTemperature.COLD);
    }

    @Test
    void qualifiedLeadWithModerateContactIsWarm() {
        Lead lead = Lead.builder()
                .source(LeadSource.SOCIAL_MEDIA)
                .status(LeadStatus.QUALIFIED)
                .contactCount(2)
                .build();

        int score = LeadScoringService.computeScore(lead);

        assertThat(score).isBetween(40, 69);
        assertThat(LeadScoringService.temperatureFor(score, lead)).isEqualTo(LeadTemperature.WARM);
    }

    @Test
    void scoreNeverExceedsHundredOrDropsBelowZero() {
        Lead lead = Lead.builder().source(LeadSource.REFERRAL).status(LeadStatus.CONVERTED).contactCount(50).build();
        assertThat(LeadScoringService.computeScore(lead)).isBetween(0, 100);
    }
}
