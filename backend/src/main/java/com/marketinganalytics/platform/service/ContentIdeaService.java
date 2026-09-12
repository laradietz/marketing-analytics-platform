package com.marketinganalytics.platform.service;

import com.marketinganalytics.platform.dto.common.PageResponse;
import com.marketinganalytics.platform.dto.content.ContentGenerationRequest;
import com.marketinganalytics.platform.dto.content.ContentIdeaResponse;
import com.marketinganalytics.platform.dto.content.ContentIdeaUpdateRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ContentIdeaService {
    List<ContentIdeaResponse> generate(ContentGenerationRequest request);
    PageResponse<ContentIdeaResponse> list(Pageable pageable);
    ContentIdeaResponse update(Long id, ContentIdeaUpdateRequest request);
    void delete(Long id);
}
