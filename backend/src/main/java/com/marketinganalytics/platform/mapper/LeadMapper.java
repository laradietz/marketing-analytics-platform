package com.marketinganalytics.platform.mapper;

import com.marketinganalytics.platform.dto.lead.LeadResponse;
import com.marketinganalytics.platform.entity.Lead;
import com.marketinganalytics.platform.service.LeadScoringService;

public final class LeadMapper {

    private LeadMapper() {
    }

    public static LeadResponse toResponse(Lead lead) {
        return new LeadResponse(
                lead.getId(),
                lead.getName(),
                lead.getEmail(),
                lead.getPhone(),
                lead.getCompany(),
                lead.getSource(),
                lead.getStatus(),
                lead.getScore(),
                lead.getContactCount(),
                LeadScoringService.temperatureFor(lead.getScore(), lead),
                lead.getCampaign() != null ? lead.getCampaign().getId() : null,
                lead.getCampaign() != null ? lead.getCampaign().getName() : null,
                lead.getAssignedTo() != null ? lead.getAssignedTo().getId() : null,
                lead.getAssignedTo() != null ? lead.getAssignedTo().getName() : null,
                lead.getNotes(),
                lead.getCreatedAt(),
                lead.getUpdatedAt()
        );
    }
}
