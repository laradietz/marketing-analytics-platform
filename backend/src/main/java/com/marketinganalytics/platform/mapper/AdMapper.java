package com.marketinganalytics.platform.mapper;

import com.marketinganalytics.platform.dto.ad.AdResponse;
import com.marketinganalytics.platform.entity.Ad;

public final class AdMapper {

    private AdMapper() {
    }

    public static AdResponse toResponse(Ad ad) {
        return new AdResponse(
                ad.getId(), ad.getCampaign().getId(), ad.getName(), ad.getFormat(),
                ad.getHeadline(), ad.getBody(), ad.getCtaLabel(), ad.getStatus(), ad.getCreatedAt());
    }
}
