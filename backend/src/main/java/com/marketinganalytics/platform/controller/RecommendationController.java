package com.marketinganalytics.platform.controller;

import com.marketinganalytics.platform.dto.common.PageResponse;
import com.marketinganalytics.platform.dto.recommendation.RecommendationResponse;
import com.marketinganalytics.platform.entity.enums.RecommendationStatus;
import com.marketinganalytics.platform.service.RecommendationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Tag(name = "Campaign Optimizer")
@SecurityRequirement(name = "bearerAuth")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping("/generate")
    public List<RecommendationResponse> generate() {
        return recommendationService.generate();
    }

    @GetMapping
    public PageResponse<RecommendationResponse> list(@PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return recommendationService.list(pageable);
    }

    @PatchMapping("/{id}/status")
    public RecommendationResponse updateStatus(@PathVariable Long id, @RequestParam RecommendationStatus status) {
        return recommendationService.updateStatus(id, status);
    }
}
