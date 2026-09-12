package com.marketinganalytics.platform.service;

import com.marketinganalytics.platform.dto.campaign.CampaignFilter;
import com.marketinganalytics.platform.dto.campaign.CampaignRequest;
import com.marketinganalytics.platform.dto.campaign.CampaignResponse;
import com.marketinganalytics.platform.dto.common.PageResponse;
import org.springframework.data.domain.Pageable;

public interface CampaignService {
    PageResponse<CampaignResponse> list(CampaignFilter filter, Pageable pageable);
    CampaignResponse getById(Long id);
    CampaignResponse create(CampaignRequest request);
    CampaignResponse update(Long id, CampaignRequest request);
    void delete(Long id);
    CampaignResponse activate(Long id);
    CampaignResponse pause(Long id);
    CampaignResponse complete(Long id);
    CampaignResponse cancel(Long id);
}
