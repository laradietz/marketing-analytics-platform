package com.marketinganalytics.platform.dto.comment;

import java.util.List;

public record CommentImportResult(int totalRows, int imported, List<RowError> errors) {

    public record RowError(int row, String message) {
    }
}
