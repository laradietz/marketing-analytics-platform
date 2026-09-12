package com.marketinganalytics.platform.dto.dashboard;

import com.marketinganalytics.platform.entity.enums.CampaignObjective;

import java.time.LocalDate;

public record DashboardFilter(
        LocalDate from,
        LocalDate to,
        Long platformId,
        Long campaignId,
        CampaignObjective objective
) {
}
