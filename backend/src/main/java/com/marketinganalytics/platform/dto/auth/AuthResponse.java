package com.marketinganalytics.platform.dto.auth;

import com.marketinganalytics.platform.dto.user.UserResponse;

public record AuthResponse(
        String token,
        String tokenType,
        UserResponse user
) {
    public static AuthResponse of(String token, UserResponse user) {
        return new AuthResponse(token, "Bearer", user);
    }
}
