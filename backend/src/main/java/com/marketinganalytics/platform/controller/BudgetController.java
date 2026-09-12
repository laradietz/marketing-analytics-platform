package com.marketinganalytics.platform.controller;

import com.marketinganalytics.platform.dto.budget.BudgetRequest;
import com.marketinganalytics.platform.dto.budget.BudgetResponse;
import com.marketinganalytics.platform.service.BudgetService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns/{campaignId}/budgets")
@RequiredArgsConstructor
@Tag(name = "Presupuestos de campaña")
@SecurityRequirement(name = "bearerAuth")
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public List<BudgetResponse> list(@PathVariable Long campaignId) {
        return budgetService.listByCampaign(campaignId);
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> create(@PathVariable Long campaignId, @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetService.create(campaignId, request));
    }

    @DeleteMapping("/{budgetId}")
    public ResponseEntity<Void> delete(@PathVariable Long campaignId, @PathVariable Long budgetId) {
        budgetService.delete(campaignId, budgetId);
        return ResponseEntity.noContent().build();
    }
}
