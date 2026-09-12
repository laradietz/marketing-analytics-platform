package com.marketinganalytics.platform.service;

import com.marketinganalytics.platform.dto.dashboard.DashboardFilter;
import com.marketinganalytics.platform.dto.dashboard.DashboardResponse;

public interface DashboardService {
    DashboardResponse getDashboard(DashboardFilter filter);
}
