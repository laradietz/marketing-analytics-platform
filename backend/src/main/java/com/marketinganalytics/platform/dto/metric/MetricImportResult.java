package com.marketinganalytics.platform.dto.metric;

import java.util.List;

public record MetricImportResult(int totalRows, int imported, List<RowError> errors) {

    public record RowError(int row, String message) {
    }
}
