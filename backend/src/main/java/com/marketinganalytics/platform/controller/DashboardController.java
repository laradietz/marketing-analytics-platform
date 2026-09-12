package com.marketinganalytics.platform.controller;

import com.marketinganalytics.platform.dto.dashboard.DashboardFilter;
import com.marketinganalytics.platform.dto.dashboard.DashboardResponse;
import com.marketinganalytics.platform.entity.enums.CampaignObjective;
import com.marketinganalytics.platform.service.DashboardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public DashboardResponse getDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long platformId,
            @RequestParam(required = false) Long campaignId,
            @RequestParam(required = false) CampaignObjective objective) {
        return dashboardService.getDashboard(new DashboardFilter(from, to, platformId, campaignId, objective));
    }
}
