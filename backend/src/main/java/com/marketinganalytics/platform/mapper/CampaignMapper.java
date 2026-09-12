package com.marketinganalytics.platform.mapper;

import com.marketinganalytics.platform.dto.campaign.CampaignResponse;
import com.marketinganalytics.platform.dto.metric.DerivedMetrics;
import com.marketinganalytics.platform.entity.Campaign;

public final class CampaignMapper {

    private CampaignMapper() {
    }

    public static CampaignResponse toResponse(Campaign campaign, DerivedMetrics metrics) {
        return new CampaignResponse(
                campaign.getId(),
                campaign.getName(),
                campaign.getDescription(),
                PlatformMapper.toResponse(campaign.getPlatform()),
                campaign.getObjective(),
                campaign.getBudget(),
                campaign.getStartDate(),
                campaign.getEndDate(),
                campaign.getStatus(),
                campaign.getTargetAudience(),
                campaign.getOwner() != null ? campaign.getOwner().getId() : null,
                campaign.getOwner() != null ? campaign.getOwner().getName() : null,
                campaign.getNotes(),
                campaign.getExternalCampaignId(),
                metrics,
                campaign.getCreatedAt(),
                campaign.getUpdatedAt()
        );
    }
}
