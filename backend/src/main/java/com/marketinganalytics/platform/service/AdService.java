package com.marketinganalytics.platform.service;

import com.marketinganalytics.platform.dto.ad.AdRequest;
import com.marketinganalytics.platform.dto.ad.AdResponse;

import java.util.List;

public interface AdService {
    List<AdResponse> listByCampaign(Long campaignId);
    AdResponse create(Long campaignId, AdRequest request);
    AdResponse update(Long campaignId, Long adId, AdRequest request);
    void delete(Long campaignId, Long adId);
}
