package com.marketinganalytics.platform.service;

import com.marketinganalytics.platform.dto.auth.AuthResponse;
import com.marketinganalytics.platform.dto.auth.LoginRequest;
import com.marketinganalytics.platform.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    void logout(String token);
}
