package com.marketinganalytics.platform.controller;

import com.marketinganalytics.platform.dto.metric.CampaignMetricRequest;
import com.marketinganalytics.platform.dto.metric.CampaignMetricResponse;
import com.marketinganalytics.platform.dto.metric.MetricImportResult;
import com.marketinganalytics.platform.service.CampaignMetricService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns/{campaignId}/metrics")
@RequiredArgsConstructor
@Tag(name = "Métricas de campaña")
@SecurityRequirement(name = "bearerAuth")
public class CampaignMetricController {

    private final CampaignMetricService campaignMetricService;

    @GetMapping
    public List<CampaignMetricResponse> list(@PathVariable Long campaignId) {
        return campaignMetricService.listByCampaign(campaignId);
    }

    @PostMapping
    public ResponseEntity<CampaignMetricResponse> record(@PathVariable Long campaignId,
                                                          @Valid @RequestBody CampaignMetricRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(campaignMetricService.record(campaignId, request));
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MetricImportResult importCsv(@PathVariable Long campaignId, @RequestParam("file") MultipartFile file) {
        return campaignMetricService.importCsv(campaignId, file);
    }
}
