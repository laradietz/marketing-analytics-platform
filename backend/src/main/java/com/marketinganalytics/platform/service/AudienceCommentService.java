package com.marketinganalytics.platform.service;

import com.marketinganalytics.platform.dto.comment.*;
import com.marketinganalytics.platform.dto.common.PageResponse;
import com.marketinganalytics.platform.dto.content.ContentIdeaResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AudienceCommentService {
    PageResponse<CommentResponse> list(CommentFilter filter, Pageable pageable);
    CommentResponse create(CommentRequest request);
    CommentImportResult importCsv(MultipartFile file);
    void delete(Long id);
    CommentPlatformSummary summarize(Long platformId);
    List<ContentIdeaResponse> generateIdeasFromComments(GenerateIdeasFromCommentsRequest request);
}
