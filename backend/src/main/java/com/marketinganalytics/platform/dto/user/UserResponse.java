package com.marketinganalytics.platform.dto.user;

import com.marketinganalytics.platform.entity.enums.Role;

import java.time.Instant;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        String jobTitle,
        String companyName,
        Instant createdAt
) {
}
