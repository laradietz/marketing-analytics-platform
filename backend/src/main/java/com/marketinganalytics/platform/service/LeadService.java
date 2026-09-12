package com.marketinganalytics.platform.service;

import com.marketinganalytics.platform.dto.common.PageResponse;
import com.marketinganalytics.platform.dto.lead.LeadFilter;
import com.marketinganalytics.platform.dto.lead.LeadRequest;
import com.marketinganalytics.platform.dto.lead.LeadResponse;
import com.marketinganalytics.platform.dto.lead.LeadStatusUpdateRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LeadService {
    PageResponse<LeadResponse> list(LeadFilter filter, Pageable pageable);
    LeadResponse getById(Long id);
    LeadResponse create(LeadRequest request);
    LeadResponse update(Long id, LeadRequest request);
    LeadResponse updateStatus(Long id, LeadStatusUpdateRequest request);
    LeadResponse registerContact(Long id);
    void delete(Long id);
    List<LeadResponse> getRecent();
}
