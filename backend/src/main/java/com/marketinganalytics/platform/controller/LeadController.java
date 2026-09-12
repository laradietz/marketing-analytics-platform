package com.marketinganalytics.platform.controller;

import com.marketinganalytics.platform.dto.common.PageResponse;
import com.marketinganalytics.platform.dto.lead.LeadFilter;
import com.marketinganalytics.platform.dto.lead.LeadRequest;
import com.marketinganalytics.platform.dto.lead.LeadResponse;
import com.marketinganalytics.platform.dto.lead.LeadStatusUpdateRequest;
import com.marketinganalytics.platform.entity.enums.LeadSource;
import com.marketinganalytics.platform.entity.enums.LeadStatus;
import com.marketinganalytics.platform.service.LeadService;
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
@RequestMapping("/api/leads")
@RequiredArgsConstructor
@Tag(name = "Leads")
@SecurityRequirement(name = "bearerAuth")
public class LeadController {

    private final LeadService leadService;

    @GetMapping
    public PageResponse<LeadResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LeadStatus status,
            @RequestParam(required = false) LeadSource source,
            @RequestParam(required = false) Long campaignId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return leadService.list(new LeadFilter(search, status, source, campaignId), pageable);
    }

    @GetMapping("/{id}")
    public LeadResponse getById(@PathVariable Long id) {
        return leadService.getById(id);
    }

    @PostMapping
    public ResponseEntity<LeadResponse> create(@Valid @RequestBody LeadRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leadService.create(request));
    }

    @PutMapping("/{id}")
    public LeadResponse update(@PathVariable Long id, @Valid @RequestBody LeadRequest request) {
        return leadService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public LeadResponse updateStatus(@PathVariable Long id, @Valid @RequestBody LeadStatusUpdateRequest request) {
        return leadService.updateStatus(id, request);
    }

    @PostMapping("/{id}/contact")
    public LeadResponse registerContact(@PathVariable Long id) {
        return leadService.registerContact(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        leadService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
