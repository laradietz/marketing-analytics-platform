package com.marketinganalytics.platform.exception;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldValidationError> fieldErrors
) {
    public record FieldValidationError(String field, String message) {
    }

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(Instant.now(), status, error, message, path, List.of());
    }

    public static ErrorResponse ofFieldErrors(int status, String error, String message, String path,
                                               Map<String, String> fieldErrors) {
        List<FieldValidationError> errors = fieldErrors.entrySet().stream()
                .map(e -> new FieldValidationError(e.getKey(), e.getValue()))
                .toList();
        return new ErrorResponse(Instant.now(), status, error, message, path, errors);
    }
}
