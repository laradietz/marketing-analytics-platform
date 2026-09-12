package com.marketinganalytics.platform.controller;

import com.marketinganalytics.platform.dto.ad.AdRequest;
import com.marketinganalytics.platform.dto.ad.AdResponse;
import com.marketinganalytics.platform.service.AdService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns/{campaignId}/ads")
@RequiredArgsConstructor
@Tag(name = "Ads de campaña")
@SecurityRequirement(name = "bearerAuth")
public class AdController {

    private final AdService adService;

    @GetMapping
    public List<AdResponse> list(@PathVariable Long campaignId) {
        return adService.listByCampaign(campaignId);
    }

    @PostMapping
    public ResponseEntity<AdResponse> create(@PathVariable Long campaignId, @Valid @RequestBody AdRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adService.create(campaignId, request));
    }

    @PutMapping("/{adId}")
    public AdResponse update(@PathVariable Long campaignId, @PathVariable Long adId, @Valid @RequestBody AdRequest request) {
        return adService.update(campaignId, adId, request);
    }

    @DeleteMapping("/{adId}")
    public ResponseEntity<Void> delete(@PathVariable Long campaignId, @PathVariable Long adId) {
        adService.delete(campaignId, adId);
        return ResponseEntity.noContent().build();
    }
}
