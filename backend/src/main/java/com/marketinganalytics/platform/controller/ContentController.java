package com.marketinganalytics.platform.controller;

import com.marketinganalytics.platform.dto.common.PageResponse;
import com.marketinganalytics.platform.dto.content.ContentFilter;
import com.marketinganalytics.platform.dto.content.ContentRequest;
import com.marketinganalytics.platform.dto.content.ContentResponse;
import com.marketinganalytics.platform.entity.enums.ContentStatus;
import com.marketinganalytics.platform.service.ContentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
@Tag(name = "Content Planner")
@SecurityRequirement(name = "bearerAuth")
public class ContentController {

    private final ContentService contentService;

    @GetMapping
    public PageResponse<ContentResponse> list(
            @RequestParam(required = false) ContentStatus status,
            @RequestParam(required = false) Long platformId,
            @RequestParam(required = false) Long campaignId,
            @PageableDefault(size = 20, sort = "scheduledDate") Pageable pageable) {
        return contentService.list(new ContentFilter(status, platformId, campaignId), pageable);
    }

    @GetMapping("/{id}")
    public ContentResponse getById(@PathVariable Long id) {
        return contentService.getById(id);
    }

    @PostMapping
    public ResponseEntity<ContentResponse> create(@Valid @RequestBody ContentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contentService.create(request));
    }

    @PutMapping("/{id}")
    public ContentResponse update(@PathVariable Long id, @Valid @RequestBody ContentRequest request) {
        return contentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
