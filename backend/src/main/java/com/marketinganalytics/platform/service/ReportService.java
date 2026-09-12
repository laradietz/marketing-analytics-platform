package com.marketinganalytics.platform.service;

import com.marketinganalytics.platform.dto.common.PageResponse;
import com.marketinganalytics.platform.dto.report.ReportGenerateRequest;
import com.marketinganalytics.platform.dto.report.ReportResponse;
import org.springframework.data.domain.Pageable;

public interface ReportService {
    ReportResponse generate(ReportGenerateRequest request);
    PageResponse<ReportResponse> list(Pageable pageable);
    ReportResponse getById(Long id);
    byte[] exportCsv(Long id);
}
