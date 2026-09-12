package com.marketinganalytics.platform.service.impl;

import com.marketinganalytics.platform.dto.auth.AuthResponse;
import com.marketinganalytics.platform.dto.auth.LoginRequest;
import com.marketinganalytics.platform.dto.auth.RegisterRequest;
import com.marketinganalytics.platform.entity.User;
import com.marketinganalytics.platform.entity.enums.Role;
import com.marketinganalytics.platform.exception.ConflictException;
import com.marketinganalytics.platform.mapper.UserMapper;
import com.marketinganalytics.platform.repository.UserRepository;
import com.marketinganalytics.platform.security.JwtService;
import com.marketinganalytics.platform.security.SecurityUser;
import com.marketinganalytics.platform.security.TokenBlacklistService;
import com.marketinganalytics.platform.service.ActivityLogService;
import com.marketinganalytics.platform.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final ActivityLogService activityLogService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("Ya existe una cuenta registrada con ese email");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .companyName(request.companyName())
                .active(true)
                .build();

        user = userRepository.save(user);
        activityLogService.log(user, "USER_REGISTERED", "User", user.getId(), user.getName() + " se registró en la plataforma");

        String token = jwtService.generateToken(new SecurityUser(user));
        return AuthResponse.of(token, UserMapper.toResponse(user));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado tras autenticación"));

        String token = jwtService.generateToken(new SecurityUser(user));
        return AuthResponse.of(token, UserMapper.toResponse(user));
    }

    @Override
    public void logout(String token) {
        tokenBlacklistService.revoke(token, jwtService.extractExpiration(token));
    }
}
