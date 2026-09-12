package com.marketinganalytics.platform.service;

import com.marketinganalytics.platform.dto.common.PageResponse;
import com.marketinganalytics.platform.dto.content.ContentFilter;
import com.marketinganalytics.platform.dto.content.ContentRequest;
import com.marketinganalytics.platform.dto.content.ContentResponse;
import org.springframework.data.domain.Pageable;

public interface ContentService {
    PageResponse<ContentResponse> list(ContentFilter filter, Pageable pageable);
    ContentResponse getById(Long id);
    ContentResponse create(ContentRequest request);
    ContentResponse update(Long id, ContentRequest request);
    void delete(Long id);
}
