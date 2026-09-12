package com.marketinganalytics.platform.mapper;

import com.marketinganalytics.platform.dto.platform.PlatformResponse;
import com.marketinganalytics.platform.entity.Platform;

public final class PlatformMapper {

    private PlatformMapper() {
    }

    public static PlatformResponse toResponse(Platform platform) {
        return new PlatformResponse(platform.getId(), platform.getName(), platform.getSlug(), platform.getColorHex());
    }
}
