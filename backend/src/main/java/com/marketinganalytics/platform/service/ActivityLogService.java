package com.marketinganalytics.platform.service;

import com.marketinganalytics.platform.entity.User;
import com.marketinganalytics.platform.dto.activity.ActivityLogResponse;

import java.util.List;

public interface ActivityLogService {
    void log(User user, String action, String entityType, Long entityId, String description);
    List<ActivityLogResponse> getRecent();
}
