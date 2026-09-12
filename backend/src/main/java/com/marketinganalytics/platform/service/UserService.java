package com.marketinganalytics.platform.service;

import com.marketinganalytics.platform.dto.user.ChangePasswordRequest;
import com.marketinganalytics.platform.dto.user.UpdateProfileRequest;
import com.marketinganalytics.platform.dto.user.UserResponse;

public interface UserService {
    UserResponse getCurrentProfile();
    UserResponse updateProfile(UpdateProfileRequest request);
    void changePassword(ChangePasswordRequest request);
}
