package com.marketinganalytics.platform.service;

import com.marketinganalytics.platform.dto.goal.GoalRequest;
import com.marketinganalytics.platform.dto.goal.GoalResponse;

import java.util.List;

public interface GoalService {
    List<GoalResponse> list();
    GoalResponse create(GoalRequest request);
    void delete(Long id);
}
