package com.marketinganalytics.platform.service;

import com.marketinganalytics.platform.dto.metric.CampaignMetricRequest;
import com.marketinganalytics.platform.dto.metric.CampaignMetricResponse;
import com.marketinganalytics.platform.dto.metric.MetricImportResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CampaignMetricService {
    List<CampaignMetricResponse> listByCampaign(Long campaignId);
    CampaignMetricResponse record(Long campaignId, CampaignMetricRequest request);
    MetricImportResult importCsv(Long campaignId, MultipartFile file);
}
