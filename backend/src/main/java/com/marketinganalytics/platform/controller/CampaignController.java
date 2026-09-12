package com.marketinganalytics.platform.controller;

import com.marketinganalytics.platform.dto.campaign.CampaignFilter;
import com.marketinganalytics.platform.dto.campaign.CampaignRequest;
import com.marketinganalytics.platform.dto.campaign.CampaignResponse;
import com.marketinganalytics.platform.dto.common.PageResponse;
import com.marketinganalytics.platform.entity.enums.CampaignObjective;
import com.marketinganalytics.platform.entity.enums.CampaignStatus;
import com.marketinganalytics.platform.service.CampaignService;
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
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
@Tag(name = "Campañas")
@SecurityRequirement(name = "bearerAuth")
public class CampaignController {

    private final CampaignService campaignService;

    @GetMapping
    public PageResponse<CampaignResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CampaignStatus status,
            @RequestParam(required = false) Long platformId,
            @RequestParam(required = false) CampaignObjective objective,
            @RequestParam(required = false) Long ownerId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        CampaignFilter filter = new CampaignFilter(search, status, platformId, objective, ownerId);
        return campaignService.list(filter, pageable);
    }

    @GetMapping("/{id}")
    public CampaignResponse getById(@PathVariable Long id) {
        return campaignService.getById(id);
    }

    @PostMapping
    public ResponseEntity<CampaignResponse> create(@Valid @RequestBody CampaignRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(campaignService.create(request));
    }

    @PutMapping("/{id}")
    public CampaignResponse update(@PathVariable Long id, @Valid @RequestBody CampaignRequest request) {
        return campaignService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        campaignService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public CampaignResponse activate(@PathVariable Long id) {
        return campaignService.activate(id);
    }

    @PostMapping("/{id}/pause")
    public CampaignResponse pause(@PathVariable Long id) {
        return campaignService.pause(id);
    }

    @PostMapping("/{id}/complete")
    public CampaignResponse complete(@PathVariable Long id) {
        return campaignService.complete(id);
    }

    @PostMapping("/{id}/cancel")
    public CampaignResponse cancel(@PathVariable Long id) {
        return campaignService.cancel(id);
    }
}
