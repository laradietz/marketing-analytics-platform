package com.marketinganalytics.platform.service;

import com.marketinganalytics.platform.entity.Lead;
import com.marketinganalytics.platform.entity.enums.LeadTemperature;

/**
 * Deterministic lead scoring based on source quality, contact frequency,
 * pipeline status and campaign-linked behavior. Every factor is documented
 * so the score is explainable to a sales rep, not a black box.
 */
public final class LeadScoringService {

    private LeadScoringService() {
    }

    public static int computeScore(Lead lead) {
        int score = sourceScore(lead) + contactScore(lead) + statusScore(lead) + behaviorScore(lead);
        return Math.max(0, Math.min(100, score));
    }

    public static LeadTemperature temperatureFor(int score, Lead lead) {
        if (lead.getStatus() == com.marketinganalytics.platform.entity.enums.LeadStatus.LOST) {
            return LeadTemperature.COLD;
        }
        if (score >= 70) {
            return LeadTemperature.HOT;
        }
        if (score >= 40) {
            return LeadTemperature.WARM;
        }
        return LeadTemperature.COLD;
    }

    /** Higher-intent acquisition channels score higher (max 25). */
    private static int sourceScore(Lead lead) {
        if (lead.getSource() == null) {
            return 0;
        }
        return switch (lead.getSource()) {
            case REFERRAL -> 25;
            case SOCIAL_MEDIA -> 20;
            case PAID_ADS, EVENT -> 18;
            case ORGANIC -> 15;
            case EMAIL -> 12;
            case OTHER -> 5;
        };
    }

    /** More touchpoints without going cold signal genuine interest (max 25). */
    private static int contactScore(Lead lead) {
        return Math.min(lead.getContactCount() * 7, 25);
    }

    /** Where the lead sits in the pipeline (max 40). */
    private static int statusScore(Lead lead) {
        if (lead.getStatus() == null) {
            return 0;
        }
        return switch (lead.getStatus()) {
            case NEW -> 0;
            case CONTACTED -> 12;
            case QUALIFIED -> 28;
            case CONVERTED -> 40;
            case LOST -> 0;
        };
    }

    /** A lead tied to a high-intent campaign objective behaves more like a buyer (max 10). */
    private static int behaviorScore(Lead lead) {
        if (lead.getCampaign() == null) {
            return 0;
        }
        return switch (lead.getCampaign().getObjective()) {
            case CONVERSIONS, SALES -> 10;
            case LEADS -> 7;
            default -> 4;
        };
    }
}
