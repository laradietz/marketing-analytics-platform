package com.marketinganalytics.platform.controller;

import com.marketinganalytics.platform.dto.activity.ActivityLogResponse;
import com.marketinganalytics.platform.service.ActivityLogService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
@Tag(name = "Actividad reciente")
@SecurityRequirement(name = "bearerAuth")
public class ActivityController {

    private final ActivityLogService activityLogService;

    @GetMapping
    public List<ActivityLogResponse> getRecent() {
        return activityLogService.getRecent();
    }
}
