package com.marketinganalytics.platform.dto.platform;

public record PlatformResponse(
        Long id,
        String name,
        String slug,
        String colorHex
) {
}
