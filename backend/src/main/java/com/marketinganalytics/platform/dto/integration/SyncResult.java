package com.marketinganalytics.platform.dto.integration;

import java.util.List;

public record SyncResult(int campaignsSynced, int metricsImported, List<String> errors) {
}
