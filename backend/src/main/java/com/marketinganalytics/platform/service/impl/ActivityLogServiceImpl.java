package com.marketinganalytics.platform.service.impl;

import com.marketinganalytics.platform.dto.activity.ActivityLogResponse;
import com.marketinganalytics.platform.entity.ActivityLog;
import com.marketinganalytics.platform.entity.User;
import com.marketinganalytics.platform.repository.ActivityLogRepository;
import com.marketinganalytics.platform.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    /**
     * Participates in the caller's transaction (default propagation) rather
     * than REQUIRES_NEW: a new transaction would not see rows the caller just
     * inserted but hasn't committed yet (e.g. a brand-new User during
     * registration), causing a foreign-key violation on the log entry.
     */
    @Override
    @Transactional
    public void log(User user, String action, String entityType, Long entityId, String description) {
        ActivityLog log = ActivityLog.builder()
                .user(user)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .build();
        activityLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getRecent() {
        return activityLogRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(l -> new ActivityLogResponse(
                        l.getId(),
                        l.getUser() != null ? l.getUser().getName() : "Sistema",
                        l.getAction(),
                        l.getEntityType(),
                        l.getEntityId(),
                        l.getDescription(),
                        l.getCreatedAt()))
                .toList();
    }
}
