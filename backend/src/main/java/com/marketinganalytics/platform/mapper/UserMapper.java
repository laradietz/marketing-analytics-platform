package com.marketinganalytics.platform.mapper;

import com.marketinganalytics.platform.dto.user.UserResponse;
import com.marketinganalytics.platform.entity.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getJobTitle(),
                user.getCompanyName(),
                user.getCreatedAt()
        );
    }
}
